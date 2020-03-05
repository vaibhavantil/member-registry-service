package com.hedvig.memberservice.services

import com.hedvig.external.authentication.dto.NorwegianAuthenticationResponseError
import com.hedvig.external.authentication.dto.StartNorwegianAuthenticationResult
import com.hedvig.external.bankID.bankIdTypes.OrderResponse
import com.hedvig.memberservice.entities.UnderwriterSignSessionEntity
import com.hedvig.memberservice.query.MemberRepository
import com.hedvig.memberservice.query.UnderwriterSignSessionRepository
import com.hedvig.memberservice.services.member.dto.StartSwedishSignResponse
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.Mock
import org.mockito.Mockito.verifyZeroInteractions
import org.mockito.junit.MockitoJUnitRunner
import java.util.*
import org.mockito.Mockito.`when` as whenever

@RunWith(MockitoJUnitRunner::class)
class UnderwriterSigningServiceImplTest {

    @Mock
    lateinit var underwriterSignSessionRepository: UnderwriterSignSessionRepository
    @Mock
    lateinit var swedishBankIdSigningService: SwedishBankIdSigningService
    @Mock
    lateinit var norwegianSigningService: NorwegianSigningService
    @Mock
    lateinit var memberRepository: MemberRepository

    @Captor
    lateinit var captor: ArgumentCaptor<UnderwriterSignSessionEntity>

    private lateinit var sut: UnderwriterSigningService

    @Before
    fun setup() {
        sut = UnderwriterSigningServiceImpl(underwriterSignSessionRepository, swedishBankIdSigningService, norwegianSigningService)
    }

    @Test
    fun startSwedishBankIdSignSession() {
        whenever(swedishBankIdSigningService.startSign(memberId, swedishSSN, ip, false))
            .thenReturn(StartSwedishSignResponse(
                signId = 0,
                bankIdOrderResponse = OrderResponse(
                    orderRef,
                    autoStartToken
                )
            ))

        whenever(underwriterSignSessionRepository.save(captor.capture()))
            .thenReturn(UnderwriterSignSessionEntity(underwriterSessionRef, orderRefUUID))

        val response = sut.startSwedishBankIdSignSession(underwriterSessionRef, memberId, swedishSSN, ip, false)

        assertThat(response.autoStartToken).isEqualTo(autoStartToken)
        assertThat(captor.value.signReference).isEqualTo(orderRefUUID)
        assertThat(captor.value.underwriterSignSessionReference).isEqualTo(underwriterSessionRef)
    }

    @Test
    fun startNorwegianBankIdSession() {
        whenever(norwegianSigningService.startSign(memberId, null))
            .thenReturn(StartNorwegianAuthenticationResult.Success(orderRefUUID, redirectUrl))

        whenever(underwriterSignSessionRepository.save(captor.capture()))
            .thenReturn(UnderwriterSignSessionEntity(underwriterSessionRef, orderRefUUID))

        val response = sut.startNorwegianBankIdSignSession(underwriterSessionRef, memberId, null)

        assertThat(response.redirectUrl).isEqualTo(redirectUrl)
        assertThat(captor.value.signReference).isEqualTo(orderRefUUID)
        assertThat(captor.value.underwriterSignSessionReference).isEqualTo(underwriterSessionRef)
    }

    @Test
    fun startNorwegianBankIdSessionFails() {
        whenever(norwegianSigningService.startSign(memberId, null))
            .thenReturn(StartNorwegianAuthenticationResult.Failed(listOf(NorwegianAuthenticationResponseError(1,"Some error message"))))

        val response = sut.startNorwegianBankIdSignSession(underwriterSessionRef, memberId, null)

        verifyZeroInteractions(underwriterSignSessionRepository)
        assertThat(response.errorMessages).isNotEmpty
    }

    companion object {
        private val underwriterSessionRef = UUID.randomUUID()
        private const val orderRef = "db5da518-5e2d-11ea-bc55-0242ac130003"
        private val orderRefUUID = UUID.fromString(orderRef)
        private const val autoStartToken = "autoStartToken"
        private const val memberId = 1337L
        private const val swedishSSN = "1912121212"
        private const val norwegianSSN = "12121212120"
        private const val redirectUrl = "redirect url"
        private const val ip = "1.0.0.0"
    }
}
