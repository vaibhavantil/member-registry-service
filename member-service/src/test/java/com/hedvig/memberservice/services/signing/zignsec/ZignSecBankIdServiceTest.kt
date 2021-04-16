package com.hedvig.memberservice.services.signing.zignsec

import com.hedvig.auth.services.UserService
import com.hedvig.auth.models.User
import com.hedvig.external.authentication.ZignSecAuthentication
import com.hedvig.external.authentication.dto.ZignSecAuthenticationMethod
import com.hedvig.external.authentication.dto.ZignSecAuthenticationResult
import com.hedvig.external.zignSec.repository.entitys.Identity
import com.hedvig.integration.apigateway.ApiGatewayService
import com.hedvig.memberservice.commands.InactivateMemberCommand
import com.hedvig.memberservice.commands.PopulateMemberThroughLoginDataCommand
import com.hedvig.memberservice.commands.ZignSecSuccessfulAuthenticationCommand
import com.hedvig.memberservice.commands.models.ZignSecAuthenticationMarket
import com.hedvig.memberservice.query.MemberRepository
import com.hedvig.memberservice.services.redispublisher.AuthSessionUpdatedEventStatus
import com.hedvig.memberservice.services.redispublisher.RedisEventPublisher
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import org.axonframework.commandhandling.gateway.CommandGateway
import java.time.LocalDateTime
import java.util.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class ZignSecBankIdServiceTest {

    @MockK
    lateinit var zignSecAuthentication: ZignSecAuthentication

    @MockK
    lateinit var commandGateway: CommandGateway

    @MockK
    lateinit var redisEventPublisher: RedisEventPublisher

    @MockK
    lateinit var apiGatewayService: ApiGatewayService

    @MockK
    lateinit var memberRepository: MemberRepository

    @MockK
    lateinit var userService: UserService

    lateinit var classUnderTest: ZignSecBankIdService

    @BeforeEach
    fun before() {
        MockKAnnotations.init(this, relaxed = true)
        classUnderTest = ZignSecBankIdService(
            zignSecAuthentication,
            commandGateway,
            redisEventPublisher,
            apiGatewayService,
            memberRepository,
            userService,
            "success",
            "fail",
            "https://www.hedvig.com"
        )
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

        every {
            userService.findOrCreateUserWithCredentials(
                UserService.Credentials.ZignSec(
                    countryCode = "NO",
                    idProviderName = "BankIDNO",
                    idProviderPersonId = "9578-6000-4-365161",
                    personalNumber = SSN
                ), onboardingMemberId = MEMBER_ID.toString()
            )
        } returns user

        classUnderTest.completeAuthentication(result)

        verify {
            commandGateway.sendAndWait(InactivateMemberCommand(MEMBER_ID))
        }
        verify {
            apiGatewayService.reassignMember(MEMBER_ID, MEMBERS_ORIGIGINAL_ID)
        }
        verify {
            commandGateway.sendAndWait(
                ZignSecSuccessfulAuthenticationCommand(
                    MEMBERS_ORIGIGINAL_ID,
                    RESULT_ID,
                    SSN,
                    ZignSecAuthenticationMarket.NORWAY,
                    "Test",
                    "Testsson"
                )
            )
        }
        verify {
            redisEventPublisher.onAuthSessionUpdated(MEMBER_ID, AuthSessionUpdatedEventStatus.SUCCESS)
        }
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

        every {
            userService.findOrCreateUserWithCredentials(
                UserService.Credentials.ZignSec(
                    countryCode = "NO",
                    idProviderName = "BankIDNO",
                    idProviderPersonId = "9578-6000-4-365161",
                    personalNumber = SSN
                ), onboardingMemberId = MEMBER_ID.toString()
            )
        } returns user

        classUnderTest.completeAuthentication(result)

        verify {
            commandGateway.sendAndWait<Any>(
                ZignSecSuccessfulAuthenticationCommand(
                    MEMBER_ID,
                    RESULT_ID,
                    SSN,
                    ZignSecAuthenticationMarket.NORWAY,
                    "Test",
                    "Testsson"
                )
            )
        }
        verify(inverse = true) {
            apiGatewayService.reassignMember(any(), any())
        }
        verify {
            redisEventPublisher.onAuthSessionUpdated(MEMBER_ID, AuthSessionUpdatedEventStatus.SUCCESS)
        }
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

        every {
            userService.findOrCreateUserWithCredentials(
                UserService.Credentials.ZignSec(
                    countryCode = "NO",
                    idProviderName = "BankIDNO",
                    idProviderPersonId = "9578-6000-4-365161",
                    personalNumber = SSN
                ), onboardingMemberId = MEMBER_ID.toString()
            )
        } returns null

        classUnderTest.completeAuthentication(result)

        verify { redisEventPublisher.onAuthSessionUpdated(MEMBER_ID, AuthSessionUpdatedEventStatus.FAILED) }
    }

    @Test
    fun completeCompletedAuthentication_success_populatesMemberData() {
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
        every {
            userService.findOrCreateUserWithCredentials(
                UserService.Credentials.ZignSec(
                    countryCode = "NO",
                    idProviderName = "BankIDNO",
                    idProviderPersonId = "9578-6000-4-365161",
                    personalNumber = SSN
                ), onboardingMemberId = MEMBER_ID.toString()
            )
        } returns user

        classUnderTest.completeAuthentication(result)

        verify {
            commandGateway.sendAndWait(
                PopulateMemberThroughLoginDataCommand(MEMBER_ID, "Test", "Testsson")
            )
        }
    }

    @Test
    fun completeFailedAuthentication_sameMemberId_doesNotInactivateMemberAndDoesNotReassignsMember() {
        val result = ZignSecAuthenticationResult.Failed(
            RESULT_ID,
            MEMBER_ID
        )

        classUnderTest.completeAuthentication(result)

        verify { redisEventPublisher.onAuthSessionUpdated(MEMBER_ID, AuthSessionUpdatedEventStatus.FAILED) }
    }

    companion object {
        private val RESULT_ID = UUID.randomUUID()
        private val MEMBER_ID = 1337L
        private val MEMBERS_ORIGIGINAL_ID = 1338L
        private val SSN = "12121212120"
    }
}
