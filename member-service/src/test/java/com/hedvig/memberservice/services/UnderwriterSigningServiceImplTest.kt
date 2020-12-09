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
import com.hedvig.memberservice.query.saveOrUpdateReusableSession
import com.hedvig.memberservice.services.member.dto.StartSwedishSignResponse
import com.hedvig.memberservice.services.signing.simple.SimpleSigningService
import com.hedvig.memberservice.services.signing.sweden.SwedishBankIdSigningService
import com.hedvig.memberservice.services.signing.underwriter.UnderwriterSigningServiceImpl
import com.hedvig.memberservice.services.signing.zignsec.ZignSecSigningService
import io.mockk.Called
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.AssertionsForClassTypes
import org.junit.jupiter.api.Test
import java.util.*

class UnderwriterSigningServiceImplTest {

    private val underwriterSignSessionRepository: UnderwriterSignSessionRepository = mockk(relaxed = true)
    private val underwriterClient: UnderwriterClient = mockk()
    private val swedishBankIdSigningService: SwedishBankIdSigningService = mockk()
    private val zignSecSigningService: ZignSecSigningService = mockk()
    private val simpleSigningService: SimpleSigningService = mockk()
    private val signedMemberRepository: SignedMemberRepository = mockk()

    private val sut = UnderwriterSigningServiceImpl(
        underwriterSignSessionRepository,
        underwriterClient,
        swedishBankIdSigningService,
        zignSecSigningService,
        simpleSigningService,
        signedMemberRepository,
        arrayOf("hedvig.com")
    )

    @Test
    fun startSwedishBankIdSignSession() {
        every {
            signedMemberRepository.findBySsn(swedishSSN)
        } returns Optional.empty()
        every {
            swedishBankIdSigningService.startSign(memberId, swedishSSN, ip, false)
        } returns StartSwedishSignResponse(
            signId = 0,
            bankIdOrderResponse = OrderResponse(
                orderRef,
                autoStartToken
            )
        )

        val underwriterSessionRefSlot = slot<UUID>()
        val signReferenceSlot = slot<UUID>()
        every {
            underwriterSignSessionRepository.saveOrUpdateReusableSession(capture(underwriterSessionRefSlot), capture(signReferenceSlot))
        } returns Unit

        val response = sut.startSwedishBankIdSignSession(underwriterSessionRef, memberId, swedishSSN, ip, false)

        assertThat(response.autoStartToken).isEqualTo(autoStartToken)
        assertThat(signReferenceSlot.captured).isEqualTo(orderRefUUID)
        assertThat(underwriterSessionRefSlot.captured).isEqualTo(underwriterSessionRef)
    }

    @Test
    fun dontStartSwedishBankIdSignIfAlreadySigned() {
        every { signedMemberRepository.findBySsn(swedishSSN) } returns Optional.of(SignedMemberEntity())

        sut.startSwedishBankIdSignSession(underwriterSessionRef, memberId, swedishSSN, ip, false)

        verify { swedishBankIdSigningService wasNot Called }
        verify { underwriterSignSessionRepository wasNot Called }
    }

    @Test
    fun startNorwegianBankIdSession() {
        every { signedMemberRepository.findBySsn(norwegianSSN) } returns Optional.empty()
        every {
            zignSecSigningService.startSign(memberId, norwegianSSN, successTargetUrl, failUrl, ZignSecAuthenticationMarket.NORWAY)
        } returns StartZignSecAuthenticationResult.Success(orderRefUUID, redirectUrl)

        val underwriterSessionRefSlot = slot<UUID>()
        val signReferenceSlot = slot<UUID>()
        every {
            underwriterSignSessionRepository.saveOrUpdateReusableSession(capture(underwriterSessionRefSlot), capture(signReferenceSlot))
        } returns Unit

        val response = sut.startNorwegianBankIdSignSession(underwriterSessionRef, memberId, norwegianSSN, successTargetUrl, failUrl)

        assertThat(response.redirectUrl).isEqualTo(redirectUrl)
        assertThat(signReferenceSlot.captured).isEqualTo(orderRefUUID)
        assertThat(underwriterSessionRefSlot.captured).isEqualTo(underwriterSessionRef)
    }

    @Test
    fun startNorwegianBankIdSessionFails() {
        every { signedMemberRepository.findBySsn(norwegianSSN) } returns Optional.empty()
        every {
            zignSecSigningService.startSign(memberId, norwegianSSN, successTargetUrl, failUrl, ZignSecAuthenticationMarket.NORWAY)
        } returns StartZignSecAuthenticationResult.Failed(listOf(ZignSecAuthenticationResponseError(1, "Some error message")))

        val response = sut.startNorwegianBankIdSignSession(underwriterSessionRef, memberId, norwegianSSN, successTargetUrl, failUrl)

        verify { underwriterSignSessionRepository wasNot Called }
        assertThat(response.errorMessages).isNotEmpty
    }

    @Test
    fun dontStartNorwegianBankIdSignIfAlreadySigned() {
        every { signedMemberRepository.findBySsn(norwegianSSN) } returns Optional.of(SignedMemberEntity())

        sut.startNorwegianBankIdSignSession(underwriterSessionRef, memberId, norwegianSSN, successTargetUrl, failUrl)

        verify { swedishBankIdSigningService wasNot Called }
        verify { underwriterSignSessionRepository wasNot Called }
    }

    @Test
    fun underwriterSignSessionExists_thenShouldBeHandled() {
        every { underwriterSignSessionRepository.findBySignReference(orderRefUUID) } returns UnderwriterSignSessionEntity(underwriterSessionRef, orderRefUUID)

        val handled = sut.isUnderwriterHandlingSignSession(orderRefUUID)

        assertThat(handled)
    }

    @Test
    fun underwriterSignSessionDoseNotExists_thenShouldNotBeHandled() {
        every { underwriterSignSessionRepository.findBySignReference(orderRefUUID) } returns null

        val handled = sut.isUnderwriterHandlingSignSession(orderRefUUID)

        assertThat(!handled)
    }

    @Test
    fun failStartNorwegianBankIdSignSessionOnInvalidHost() {
        val response = sut.startNorwegianBankIdSignSession(orderRefUUID, memberId, norwegianSSN, "http://someOther.host", "http://someOther.host")

        assertThat(response.redirectUrl).isNull()
        assertThat(response.internalErrorMessage).isNotNull()
    }

    @Test
    fun `startDanishBankIdSignSession on already signed member should return internal error message`() {
        every {
            signedMemberRepository.findBySsn(any())
        } returns Optional.of(SignedMemberEntity())

        val response = sut.startDanishBankIdSignSession(UUID.randomUUID(), 123L, "", "https://hedvig.com/success", "https://hedvig.com/fail")

        AssertionsForClassTypes.assertThat(response.redirectUrl).isNull()
        AssertionsForClassTypes.assertThat(response.internalErrorMessage).isNotNull()
    }

    @Test
    fun `startSimpleSignSession on already signed member should return internal error message`() {
        every {
            signedMemberRepository.findBySsn(any())
        } returns Optional.of(SignedMemberEntity())

        val response = sut.startSimpleSignSession(UUID.randomUUID(), 123L, "")

        AssertionsForClassTypes.assertThat(response.successfullyStarted).isFalse()
        AssertionsForClassTypes.assertThat(response.internalErrorMessage).isNotNull()
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
