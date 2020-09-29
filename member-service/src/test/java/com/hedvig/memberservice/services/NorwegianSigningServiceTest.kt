package com.hedvig.memberservice.services

import com.hedvig.external.authentication.dto.ZignSecAuthenticationResponseError
import com.hedvig.external.authentication.dto.ZignSecSignResult
import com.hedvig.external.authentication.dto.StartZignSecAuthenticationResult
import com.hedvig.memberservice.services.events.SignSessionCompleteEvent
import com.hedvig.memberservice.services.member.MemberService
import com.hedvig.memberservice.services.redispublisher.RedisEventPublisher
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
    lateinit var memberService: MemberService

    @Mock
    lateinit var norwegianBankIdService: NorwegianBankIdService

    @Mock
    lateinit var applicationEventPublisher: ApplicationEventPublisher

    @Mock
    lateinit var redisEventPublisher: RedisEventPublisher

    lateinit var classUnderTest: NorwegianSigningService

    @Before
    fun before() {
        classUnderTest = NorwegianSigningService(memberService, norwegianBankIdService, applicationEventPublisher, redisEventPublisher)
    }

    @Test
    fun startSignSuccessful() {
        whenever(norwegianBankIdService.sign(MEMBER_ID.toString(), SSN, SUCCESS_TARGET_URL, FAILED_TARGET_URL)).thenReturn(
            StartZignSecAuthenticationResult.Success(
                ORDER_REF,
                REDIRECT_URL
            )
        )

        val response = classUnderTest.startSign(MEMBER_ID, SSN, SUCCESS_TARGET_URL, FAILED_TARGET_URL)

        assertThat(response).isInstanceOf(StartZignSecAuthenticationResult.Success::class.java)
        assertThat((response as StartZignSecAuthenticationResult.Success).redirectUrl).isEqualTo(REDIRECT_URL)
    }

    @Test
    fun startSignFails() {
        whenever(norwegianBankIdService.sign(MEMBER_ID.toString(), SSN, SUCCESS_TARGET_URL, FAILED_TARGET_URL)).thenReturn(
            StartZignSecAuthenticationResult.Failed(
                LIST_OF_ERRORS
            )
        )

        val response = classUnderTest.startSign(MEMBER_ID, SSN, SUCCESS_TARGET_URL, FAILED_TARGET_URL)

        assertThat(response).isInstanceOf(StartZignSecAuthenticationResult.Failed::class.java)
    }

    @Test
    fun handleSuccessfulSigning() {
        classUnderTest.handleSignResult(
            ZignSecSignResult.Signed(
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
            ZignSecSignResult.Failed(
                RESPONSE_ID,
                MEMBER_ID
            )
        )

        verifyZeroInteractions(memberService)
        verify(applicationEventPublisher).publishEvent(SignSessionCompleteEvent(MEMBER_ID))
    }


    companion object {
        private const val MEMBER_ID: Long = 1337
        private const val SSN: String = "12121212120"
        private const val EMAIL: String = "em@i.l"
        private const val IP_ADDRESS: String = ""
        private const val REDIRECT_URL: String = "redirect_url"
        private const val PROVIDER_JSON_RESPONSE = """{ "json": true }"""
        private const val SUCCESS_TARGET_URL = "success"
        private const val FAILED_TARGET_URL = "failed"
        private val LIST_OF_ERRORS = listOf(ZignSecAuthenticationResponseError(0, "some error"))
        private val RESPONSE_ID: UUID = UUID.randomUUID()
        private val ORDER_REF: UUID = UUID.randomUUID()
    }
}
