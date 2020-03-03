package com.hedvig.memberservice.services

import com.hedvig.external.authentication.NorwegianAuthentication
import com.hedvig.external.authentication.dto.NorwegianAuthenticationResult
import com.hedvig.integration.apigateway.ApiGatewayService
import com.hedvig.memberservice.commands.InactivateMemberCommand
import com.hedvig.memberservice.query.MemberRepository
import com.hedvig.memberservice.query.SignedMemberEntity
import com.hedvig.memberservice.query.SignedMemberRepository
import com.hedvig.memberservice.services.redispublisher.AuthSessionUpdatedEventStatus
import com.hedvig.memberservice.services.redispublisher.RedisEventPublisher
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
class NorwegianBankIdServiceTest {

    @Mock
    lateinit var norwegianAuthentication: NorwegianAuthentication
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

    lateinit var classUnderTest: NorwegianBankIdService

    @Before
    fun before() {
        classUnderTest = NorwegianBankIdService(norwegianAuthentication, commandGateway, redisEventPublisher, signedMemberRepository, apiGatewayService, memberRepository)
    }

    @Test
    fun completeCompletedAuthentication_differntMemberId_inactivateMemberAndReassignsMember() {
        val result = NorwegianAuthenticationResult.Completed(
            RESULT_ID,
            MEMBER_ID,
            SSN
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
        val result = NorwegianAuthenticationResult.Completed(
            RESULT_ID,
            MEMBER_ID,
            SSN
        )

        val signedMemberEntity = SignedMemberEntity()
        signedMemberEntity.id = MEMBER_ID
        signedMemberEntity.ssn = SSN

        whenever(signedMemberRepository.findBySsn(SSN)).thenReturn(Optional.of(
            signedMemberEntity
        ))

        classUnderTest.completeAuthentication(result)

        verify(commandGateway, never()).sendAndWait<Any>(any())
        verify(apiGatewayService, never()).reassignMember(anyLong(), anyLong())
        verify(redisEventPublisher).onAuthSessionUpdated(MEMBER_ID, AuthSessionUpdatedEventStatus.SUCCESS)
    }

    @Test
    fun completeCompletedAuthentication_noSignedMember_publishFailedEvent() {
        val result = NorwegianAuthenticationResult.Completed(
            RESULT_ID,
            MEMBER_ID,
            SSN
        )

        whenever(signedMemberRepository.findBySsn(SSN)).thenReturn(Optional.empty())

        classUnderTest.completeAuthentication(result)

        verify(redisEventPublisher).onAuthSessionUpdated(MEMBER_ID, AuthSessionUpdatedEventStatus.FAILED)
    }


    @Test
    fun completeFailedAuthentication_sameMemberId_doesNotInactivateMemberAndDoesNotReassignsMember() {
        val result = NorwegianAuthenticationResult.Failed(
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
