package com.hedvig.memberservice.services.signing.zignsec

import com.hedvig.auth.services.UserService
import com.hedvig.auth.models.User
import com.hedvig.external.authentication.ZignSecAuthentication
import com.hedvig.external.authentication.dto.ZignSecAuthenticationMethod
import com.hedvig.external.authentication.dto.ZignSecAuthenticationResult
import com.hedvig.external.zignSec.repository.entitys.Identity
import com.hedvig.integration.apigateway.ApiGatewayService
import com.hedvig.memberservice.commands.InactivateMemberCommand
import com.hedvig.memberservice.commands.ZignSecSuccessfulAuthenticationCommand
import com.hedvig.memberservice.commands.models.ZignSecAuthenticationMarket
import com.hedvig.memberservice.query.MemberRepository
import com.hedvig.memberservice.services.redispublisher.AuthSessionUpdatedEventStatus
import com.hedvig.memberservice.services.redispublisher.RedisEventPublisher
import org.axonframework.commandhandling.gateway.CommandGateway
import org.junit.Test
import org.junit.Before
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.anyLong
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.junit.MockitoJUnitRunner
import org.springframework.core.env.Environment
import java.time.LocalDateTime
import java.util.*
import org.mockito.Mockito.`when` as whenever

@RunWith(MockitoJUnitRunner::class)
class ZignSecBankIdServiceTest {

    @Mock
    lateinit var zignSecAuthentication: ZignSecAuthentication

    @Mock
    lateinit var commandGateway: CommandGateway

    @Mock
    lateinit var redisEventPublisher: RedisEventPublisher

    @Mock
    lateinit var apiGatewayService: ApiGatewayService

    @Mock
    lateinit var memberRepository: MemberRepository

    @Mock
    lateinit var userService: UserService

    @Mock
    lateinit var env: Environment

    lateinit var classUnderTest: ZignSecBankIdService

    @Before
    fun before() {
        classUnderTest = ZignSecBankIdService(zignSecAuthentication, commandGateway, redisEventPublisher, apiGatewayService, memberRepository, userService, "success", "fail", "https://www.hedvig.com")
    }

    @Test
    fun completeCompletedAuthentication_differentMemberId_inactivateMemberAndReassignsMember() {
        val result = ZignSecAuthenticationResult.Completed(
            Identity(
                countryCode = "NO",
                firstName = "Test",
                lastName = "Testsson",
                fullName = "Test Testsson",
                personalNumber = null,
                dateOfBirth = "1900-01-01",
                age = 121,
                idProviderName = "BankIDNO",
                identificationDate = LocalDateTime.now(),
                idProviderRequestId = null,
                idProviderPersonId = "9578-6000-4-365161",
                customerPersonId = null
            ),
            RESULT_ID,
            MEMBER_ID,
            SSN,
            ZignSecAuthenticationMethod.NORWAY_WEB_OR_MOBILE
        )

        val user = User(associatedMemberId = MEMBERS_ORIGIGINAL_ID.toString())

        whenever(userService.findOrCreateUserWithCredential(
            UserService.Credential.ZignSec(
                idProviderName = "BankIDNO",
                idProviderPersonId = "9578-6000-4-365161",
                simpleSignFallback = UserService.Credential.SimpleSign(
                    countryCode = "NO",
                    personalNumber = SSN
                )
            ), UserService.Context(
                onboardingMemberId = MEMBER_ID.toString()
            ))).thenReturn(user)

        classUnderTest.completeAuthentication(result)

        verify(commandGateway).sendAndWait<Any>(InactivateMemberCommand(MEMBER_ID))
        verify(apiGatewayService).reassignMember(MEMBER_ID, MEMBERS_ORIGIGINAL_ID)
        verify(commandGateway).sendAndWait<Any>(ZignSecSuccessfulAuthenticationCommand(MEMBERS_ORIGIGINAL_ID, RESULT_ID, SSN, ZignSecAuthenticationMarket.NORWAY, "Test", "Testsson"))
        verify(redisEventPublisher).onAuthSessionUpdated(MEMBER_ID, AuthSessionUpdatedEventStatus.SUCCESS)
    }

    @Test
    fun completeCompletedAuthentication_sameMemberId_doesNotInactivateMemberAndDoesNotReassignsMember() {
        val result = ZignSecAuthenticationResult.Completed(
            Identity(
                countryCode = "NO",
                firstName = "Test",
                lastName = "Testsson",
                fullName = "Test Testsson",
                personalNumber = null,
                dateOfBirth = "1900-01-01",
                age = 121,
                idProviderName = "BankIDNO",
                identificationDate = LocalDateTime.now(),
                idProviderRequestId = null,
                idProviderPersonId = "9578-6000-4-365161",
                customerPersonId = null
            ),
            RESULT_ID,
            MEMBER_ID,
            SSN,
            ZignSecAuthenticationMethod.NORWAY_WEB_OR_MOBILE
        )

        val user = User(associatedMemberId = MEMBER_ID.toString())

        whenever(userService.findOrCreateUserWithCredential(
            UserService.Credential.ZignSec(
                idProviderName = "BankIDNO",
                idProviderPersonId = "9578-6000-4-365161",
                simpleSignFallback = UserService.Credential.SimpleSign(
                    countryCode = "NO",
                    personalNumber = SSN
                )
            ), UserService.Context(
                onboardingMemberId = MEMBER_ID.toString()
            ))).thenReturn(user)

        classUnderTest.completeAuthentication(result)

        verify(commandGateway).sendAndWait<Any>(
            ZignSecSuccessfulAuthenticationCommand(
                MEMBER_ID,
                RESULT_ID,
                SSN,
                ZignSecAuthenticationMarket.NORWAY,
                "Test",
                "Testsson"
            )
        )
        verify(apiGatewayService, never()).reassignMember(anyLong(), anyLong())
        verify(redisEventPublisher).onAuthSessionUpdated(MEMBER_ID, AuthSessionUpdatedEventStatus.SUCCESS)
    }

    @Test
    fun completeCompletedAuthentication_noSignedMember_publishFailedEvent() {
        val result = ZignSecAuthenticationResult.Completed(
            Identity(
                countryCode = "NO",
                firstName = "Test",
                lastName = "Testsson",
                fullName = "Test Testsson",
                personalNumber = null,
                dateOfBirth = "1900-01-01",
                age = 121,
                idProviderName = "BankIDNO",
                identificationDate = LocalDateTime.now(),
                idProviderRequestId = null,
                idProviderPersonId = "9578-6000-4-365161",
                customerPersonId = null
            ),
            RESULT_ID,
            MEMBER_ID,
            SSN,
            ZignSecAuthenticationMethod.NORWAY_WEB_OR_MOBILE
        )

        whenever(userService.findOrCreateUserWithCredential(
            UserService.Credential.ZignSec(
                idProviderName = "BankIDNO",
                idProviderPersonId = SSN
            ), UserService.Context(
                onboardingMemberId = MEMBER_ID.toString(),
                authType = UserService.AuthType.SIGN
            ))).thenReturn(null)

        classUnderTest.completeAuthentication(result)

        verify(redisEventPublisher).onAuthSessionUpdated(MEMBER_ID, AuthSessionUpdatedEventStatus.FAILED)
    }


    @Test
    fun completeFailedAuthentication_sameMemberId_doesNotInactivateMemberAndDoesNotReassignsMember() {
        val result = ZignSecAuthenticationResult.Failed(
            RESULT_ID,
            MEMBER_ID
        )

        classUnderTest.completeAuthentication(result)

        verify(redisEventPublisher).onAuthSessionUpdated(MEMBER_ID, AuthSessionUpdatedEventStatus.FAILED)
    }

    companion object {
        private val RESULT_ID = UUID.randomUUID()
        private val MEMBER_ID = 1337L
        private val MEMBERS_ORIGIGINAL_ID = 1338L
        private val SSN = "12121212120"
    }
}
