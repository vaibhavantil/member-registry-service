package com.hedvig.memberservice.services

import com.hedvig.external.authentication.ZignSecAuthentication
import com.hedvig.external.authentication.dto.ZignSecAuthenticationMethod
import com.hedvig.external.authentication.dto.ZignSecAuthenticationResult
import com.hedvig.integration.apigateway.ApiGatewayService
import com.hedvig.memberservice.commands.InactivateMemberCommand
import com.hedvig.memberservice.commands.ZignSecSuccessfulAuthenticationCommand
import com.hedvig.memberservice.commands.models.ZignSecAuthenticationMarket
import com.hedvig.memberservice.query.MemberRepository
import com.hedvig.memberservice.query.SignedMemberEntity
import com.hedvig.memberservice.query.SignedMemberRepository
import com.hedvig.memberservice.services.redispublisher.AuthSessionUpdatedEventStatus
import com.hedvig.memberservice.services.redispublisher.RedisEventPublisher
import com.hedvig.memberservice.services.signing.zignsec.ZignSecBankIdService
import org.axonframework.commandhandling.gateway.CommandGateway
import org.junit.Test

import org.junit.Before
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.any
import org.mockito.Mockito.anyLong
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.junit.MockitoJUnitRunner
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
    lateinit var signedMemberRepository: SignedMemberRepository

    @Mock
    lateinit var apiGatewayService: ApiGatewayService

    @Mock
    lateinit var memberRepository: MemberRepository

    lateinit var classUnderTest: ZignSecBankIdService

    @Before
    fun before() {
        classUnderTest = ZignSecBankIdService(zignSecAuthentication, commandGateway, redisEventPublisher, signedMemberRepository, apiGatewayService, memberRepository, "success", "fail")
    }

    @Test
    fun completeCompletedAuthentication_differntMemberId_inactivateMemberAndReassignsMember() {
        val result = ZignSecAuthenticationResult.Completed(
            RESULT_ID,
            MEMBER_ID,
            SSN,
            "{\"json\":true}",
            ZignSecAuthenticationMethod.NORWAY_WEB_OR_MOBILE
        )

        val signedMemberEntity = SignedMemberEntity()
        signedMemberEntity.id = MEMBERS_ORIGIGINAL_ID
        signedMemberEntity.ssn = SSN

        whenever(signedMemberRepository.findBySsn(SSN)).thenReturn(Optional.of(
            signedMemberEntity
        ))

        classUnderTest.completeAuthentication(result)

        verify(commandGateway).sendAndWait<Any>(InactivateMemberCommand(MEMBER_ID))
        verify(apiGatewayService).reassignMember(MEMBER_ID, MEMBERS_ORIGIGINAL_ID)
        verify(redisEventPublisher).onAuthSessionUpdated(MEMBER_ID, AuthSessionUpdatedEventStatus.SUCCESS)
    }

    @Test
    fun completeCompletedAuthentication_sameMemberId_doesNotInactivateMemberAndDoesNotReassignsMember() {
        val json = "{\"json\":true}"
        val result = ZignSecAuthenticationResult.Completed(
            RESULT_ID,
            MEMBER_ID,
            SSN,
            json,
            ZignSecAuthenticationMethod.NORWAY_WEB_OR_MOBILE
        )

        val signedMemberEntity = SignedMemberEntity()
        signedMemberEntity.id = MEMBER_ID
        signedMemberEntity.ssn = SSN

        whenever(signedMemberRepository.findBySsn(SSN)).thenReturn(Optional.of(
            signedMemberEntity
        ))

        classUnderTest.completeAuthentication(result)

        verify(commandGateway).sendAndWait<Any>(
            ZignSecSuccessfulAuthenticationCommand(
                MEMBER_ID,
                RESULT_ID,
                SSN,
                json,
                ZignSecAuthenticationMarket.NORWAY
            )
        )
        verify(apiGatewayService, never()).reassignMember(anyLong(), anyLong())
        verify(redisEventPublisher).onAuthSessionUpdated(MEMBER_ID, AuthSessionUpdatedEventStatus.SUCCESS)
    }

    @Test
    fun completeCompletedAuthentication_noSignedMember_publishFailedEvent() {
        val result = ZignSecAuthenticationResult.Completed(
            RESULT_ID,
            MEMBER_ID,
            SSN,
            "{\"json\":true}",
            ZignSecAuthenticationMethod.NORWAY_WEB_OR_MOBILE
        )

        whenever(signedMemberRepository.findBySsn(SSN)).thenReturn(Optional.empty())

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
