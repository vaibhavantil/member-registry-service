package com.hedvig.memberservice.services

import com.hedvig.external.authentication.dto.ZignSecAuthenticationResponseError
import com.hedvig.external.authentication.dto.StartZignSecAuthenticationResult
import com.hedvig.external.bankID.bankIdTypes.OrderResponse
import com.hedvig.integration.underwriter.UnderwriterClient
import com.hedvig.integration.underwriter.dtos.SignRequest
import com.hedvig.memberservice.commands.models.ZignSecAuthenticationMarket
import com.hedvig.memberservice.entities.UnderwriterSignSessionEntity
import com.hedvig.memberservice.query.SignedMemberEntity
import com.hedvig.memberservice.query.SignedMemberRepository
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
    lateinit var underwriterClient: UnderwriterClient
    @Mock
    lateinit var swedishBankIdSigningService: SwedishBankIdSigningService
    @Mock
    lateinit var zignSecSigningService: ZignSecSigningService
    @Mock
    lateinit var signedMemberRepository: SignedMemberRepository

    @Captor
    lateinit var captor: ArgumentCaptor<UnderwriterSignSessionEntity>
    @Captor
    lateinit var signRequestCaptor: ArgumentCaptor<SignRequest>

    private lateinit var sut: UnderwriterSigningService

    @Before
    fun setup() {
        sut = UnderwriterSigningServiceImpl(underwriterSignSessionRepository, underwriterClient, swedishBankIdSigningService, zignSecSigningService, signedMemberRepository, arrayOf("hedvig.com"))
    }

    @Test
    fun startSwedishBankIdSignSession() {
        whenever(signedMemberRepository.findBySsn(swedishSSN)).thenReturn(Optional.empty())
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
    fun dontStartSwedishBankIdSignIfAlreadySigned() {
        whenever(signedMemberRepository.findBySsn(swedishSSN)).thenReturn(Optional.of(SignedMemberEntity()))

        sut.startSwedishBankIdSignSession(underwriterSessionRef, memberId, swedishSSN, ip, false)

        verifyZeroInteractions(swedishBankIdSigningService)
        verifyZeroInteractions(underwriterSignSessionRepository)
    }

    @Test
    fun startNorwegianBankIdSession() {
        whenever(signedMemberRepository.findBySsn(norwegianSSN)).thenReturn(Optional.empty())
        whenever(zignSecSigningService.startSign(memberId, norwegianSSN, successTargetUrl, failUrl, ZignSecAuthenticationMarket.NORWAY))
            .thenReturn(StartZignSecAuthenticationResult.Success(orderRefUUID, redirectUrl))

        whenever(underwriterSignSessionRepository.save(captor.capture()))
            .thenReturn(UnderwriterSignSessionEntity(underwriterSessionRef, orderRefUUID))

        val response = sut.startNorwegianBankIdSignSession(underwriterSessionRef, memberId, norwegianSSN, successTargetUrl, failUrl)

        assertThat(response.redirectUrl).isEqualTo(redirectUrl)
        assertThat(captor.value.signReference).isEqualTo(orderRefUUID)
        assertThat(captor.value.underwriterSignSessionReference).isEqualTo(underwriterSessionRef)
    }

    @Test
    fun startNorwegianBankIdSessionFails() {
        whenever(signedMemberRepository.findBySsn(norwegianSSN)).thenReturn(Optional.empty())
        whenever(zignSecSigningService.startSign(memberId, norwegianSSN, successTargetUrl, failUrl, ZignSecAuthenticationMarket.NORWAY))
            .thenReturn(StartZignSecAuthenticationResult.Failed(listOf(ZignSecAuthenticationResponseError(1,"Some error message"))))

        val response = sut.startNorwegianBankIdSignSession(underwriterSessionRef, memberId, norwegianSSN, successTargetUrl, failUrl)

        verifyZeroInteractions(underwriterSignSessionRepository)
        assertThat(response.errorMessages).isNotEmpty
    }

    @Test
    fun dontStartNorwegianBankIdSignIfAlreadySigned() {
        whenever(signedMemberRepository.findBySsn(norwegianSSN)).thenReturn(Optional.of(SignedMemberEntity()))

        sut.startNorwegianBankIdSignSession(underwriterSessionRef, memberId, norwegianSSN, successTargetUrl, failUrl)

        verifyZeroInteractions(swedishBankIdSigningService)
        verifyZeroInteractions(underwriterSignSessionRepository)
    }

    @Test
    fun underwriterSignSessionExists_thenShouldBeHandled() {
        whenever(underwriterSignSessionRepository.findBySignReference(orderRefUUID)).thenReturn(UnderwriterSignSessionEntity(underwriterSessionRef, orderRefUUID))

        val handled = sut.isUnderwriterHandlingSignSession(orderRefUUID)

        assertThat(handled)
    }

    @Test
    fun underwriterSignSessionDoseNotExists_thenShouldNotBeHandled() {
        whenever(underwriterSignSessionRepository.findBySignReference(orderRefUUID)).thenReturn(null)

        val handled = sut.isUnderwriterHandlingSignSession(orderRefUUID)

        assertThat(!handled)
    }

    @Test
    fun failStartNorwegianBankIdSignSessionOnInvalidHost() {
        val response = sut.startNorwegianBankIdSignSession(orderRefUUID, memberId, norwegianSSN, "http://someOther.host", "http://someOther.host")

        assertThat(response.redirectUrl).isNull()
        assertThat(response.internalErrorMessage).isNotNull()
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
        private const val successTargetUrl = "https://hedvig.com/success"
        private const val failUrl = "https://hedvig.com/failed"
    }
}
