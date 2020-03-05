package com.hedvig.memberservice.services

import com.hedvig.external.authentication.dto.NorwegianAuthenticationResponseError
import com.hedvig.external.authentication.dto.NorwegianSignResult
import com.hedvig.external.authentication.dto.StartNorwegianAuthenticationResult
import com.hedvig.memberservice.entities.SignStatus
import com.hedvig.memberservice.query.MemberEntity
import com.hedvig.memberservice.query.MemberRepository
import com.hedvig.memberservice.services.events.SignSessionCompleteEvent
import com.hedvig.memberservice.services.member.MemberService
import com.hedvig.memberservice.web.v2.dto.WebsignRequest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.Before
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyZeroInteractions
import org.mockito.junit.MockitoJUnitRunner
import org.springframework.context.ApplicationEventPublisher
import java.util.*
import org.mockito.Mockito.`when` as whenever

@RunWith(MockitoJUnitRunner::class)
class NorwegianSigningServiceTest {

    @Mock
    lateinit var memberRepository: MemberRepository

    @Mock
    lateinit var memberService: MemberService

    @Mock
    lateinit var norwegianBankIdService: NorwegianBankIdService

    @Mock
    lateinit var applicationEventPublisher: ApplicationEventPublisher

    lateinit var classUnderTest: NorwegianSigningService

    @Before
    fun before() {
        classUnderTest = NorwegianSigningService(memberRepository, memberService, norwegianBankIdService, applicationEventPublisher)
    }

    @Test
    fun startSignSuccessful() {
        whenever(norwegianBankIdService.sign(MEMBER_ID.toString(), SSN)).thenReturn(
            StartNorwegianAuthenticationResult.Success(
                SESSION_ID,
                REDIRECT_URL
            )
        )

        val response = classUnderTest.startSign(MEMBER_ID, WebsignRequest(EMAIL, SSN, IP_ADDRESS))

        assertThat(response.signId).isEqualTo(SESSION_ID)
        assertThat(response.status).isEqualTo(SignStatus.IN_PROGRESS)
        assertThat(response.norwegianBankIdResponse?.redirectUrl).isEqualTo(REDIRECT_URL)
    }

    @Test
    fun startSignFails() {
        whenever(norwegianBankIdService.sign(MEMBER_ID.toString(), SSN)).thenReturn(
            StartNorwegianAuthenticationResult.Failed(
                LIST_OF_ERRORS
            )
        )

        val response = classUnderTest.startSign(MEMBER_ID, WebsignRequest(EMAIL, SSN, IP_ADDRESS))

        assertThat(response.status).isEqualTo(SignStatus.FAILED)
    }

    @Test
    fun handleSuccessfulSigning() {
        classUnderTest.handleSignResult(
            NorwegianSignResult.Signed(
                RESPONSE_ID,
                MEMBER_ID,
                SSN,
                PROVIDER_JSON_RESPONSE
            )
        )

        verify(memberService).norwegianBankIdSignComplete(MEMBER_ID, RESPONSE_ID, SSN, PROVIDER_JSON_RESPONSE)
        verify(applicationEventPublisher).publishEvent(SignSessionCompleteEvent(MEMBER_ID))
    }

    @Test
    fun handleFailedSigning() {
        classUnderTest.handleSignResult(
            NorwegianSignResult.Failed(
                RESPONSE_ID,
                MEMBER_ID
            )
        )

        verifyZeroInteractions(memberService)
        verify(applicationEventPublisher).publishEvent(SignSessionCompleteEvent(MEMBER_ID))
    }


    companion object {
        private const val SESSION_ID: Long = 1
        private const val MEMBER_ID: Long = 1337
        private const val SSN: String = "12121212120"
        private const val EMAIL: String = "em@i.l"
        private const val IP_ADDRESS: String = ""
        private const val REDIRECT_URL: String = "redirect_url"
        private const val PROVIDER_JSON_RESPONSE = """{ "json": true }"""
        private val LIST_OF_ERRORS = listOf(NorwegianAuthenticationResponseError(0, "some error"))
        private val RESPONSE_ID: UUID = UUID.randomUUID()
    }
}
