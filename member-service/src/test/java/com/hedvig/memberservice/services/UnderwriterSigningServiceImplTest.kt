package com.hedvig.memberservice.services

import com.hedvig.external.authentication.dto.ZignSecAuthenticationResponseError
import com.hedvig.external.authentication.dto.StartZignSecAuthenticationResult
import com.hedvig.external.bankID.bankIdTypes.OrderResponse
import com.hedvig.integration.underwriter.UnderwriterClient
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
import com.hedvig.memberservice.services.signing.underwriter.strategy.CommonSessionCompletion
import com.hedvig.memberservice.services.signing.underwriter.strategy.SignStrategy
import com.hedvig.memberservice.services.signing.underwriter.strategy.StartRedirectBankIdSignSessionStrategy
import com.hedvig.memberservice.services.signing.underwriter.strategy.StartSignSessionStrategyService
import com.hedvig.memberservice.services.signing.underwriter.strategy.StartSimpleSignSessionStrategy
import com.hedvig.memberservice.services.signing.underwriter.strategy.StartSwedishBankIdSignSessionStrategy
import com.hedvig.memberservice.services.signing.zignsec.ZignSecSigningService
import com.hedvig.memberservice.web.dto.NationalIdentification
import com.hedvig.memberservice.web.dto.Nationality
import com.hedvig.memberservice.web.dto.RedirectCountry
import com.hedvig.memberservice.web.dto.UnderwriterStartSignSessionRequest
import com.hedvig.memberservice.web.dto.UnderwriterStartSignSessionResponse
import io.mockk.Called
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.util.*

class UnderwriterSigningServiceImplTest {

    private val underwriterSignSessionRepository: UnderwriterSignSessionRepository = mockk(relaxed = true)
    private val underwriterClient: UnderwriterClient = mockk()
    private val commonSessionCompletion: CommonSessionCompletion = mockk()
    private val swedishBankIdSigningService: SwedishBankIdSigningService = mockk()
    private val zignSecSigningService: ZignSecSigningService = mockk()
    private val simpleSigningService: SimpleSigningService = mockk()
    private val signedMemberRepository: SignedMemberRepository = mockk()

    private val startSwedishBankIdSignSessionStrategy = StartSwedishBankIdSignSessionStrategy(
        swedishBankIdSigningService,
        underwriterClient
    )

    private val startRedirectBankIdSignSessionStrategy = StartRedirectBankIdSignSessionStrategy(
        zignSecSigningService,
        arrayOf("hedvig.com"),
        commonSessionCompletion
    )
    private val startSimpleSignSessionStrategy = StartSimpleSignSessionStrategy(
        simpleSigningService,
        commonSessionCompletion
    )

    private val startSignSessionStrategyService = StartSignSessionStrategyService(
        startSwedishBankIdSignSessionStrategy,
        startRedirectBankIdSignSessionStrategy,
        startSimpleSignSessionStrategy
    )

    private val sut = UnderwriterSigningServiceImpl(
        underwriterSignSessionRepository,
        signedMemberRepository,
        startSignSessionStrategyService
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
            underwriterSignSessionRepository.saveOrUpdateReusableSession(capture(underwriterSessionRefSlot), capture(signReferenceSlot), memberId, SignStrategy.SWEDISH_BANK_ID)
        } returns Unit

        val response = sut.startSign(
            memberId,
            UnderwriterStartSignSessionRequest.SwedishBankId(
                underwriterSessionRef,
                NationalIdentification(swedishSSN, Nationality.SWEDEN),
                ip,
                false
            )
        )

        require(response is UnderwriterStartSignSessionResponse.SwedishBankId)
        assertThat(response.autoStartToken).isEqualTo(autoStartToken)
        assertThat(signReferenceSlot.captured).isEqualTo(orderRefUUID)
        assertThat(underwriterSessionRefSlot.captured).isEqualTo(underwriterSessionRef)
    }

    @Test
    fun dontStartSwedishBankIdSignIfAlreadySigned() {
        every { signedMemberRepository.findBySsn(swedishSSN) } returns Optional.of(SignedMemberEntity())

        sut.startSign(memberId,
            UnderwriterStartSignSessionRequest.SwedishBankId(
                underwriterSessionRef,
                NationalIdentification(swedishSSN, Nationality.SWEDEN),
                ip,
                false
            )
        )

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
            underwriterSignSessionRepository.saveOrUpdateReusableSession(capture(underwriterSessionRefSlot), capture(signReferenceSlot), memberId, SignStrategy.REDIRECT_BANK_ID)
        } returns Unit

        val response = sut.startSign(
            memberId,
            UnderwriterStartSignSessionRequest.BankIdRedirect(
                underwriterSessionRef,
                NationalIdentification(norwegianSSN, Nationality.NORWAY),
                successTargetUrl,
                failUrl,
                RedirectCountry.NORWAY
            )
        )

        require(response is UnderwriterStartSignSessionResponse.BankIdRedirect)
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

        val response = sut.startSign(
            memberId,
            UnderwriterStartSignSessionRequest.BankIdRedirect(
                underwriterSessionRef,
                NationalIdentification(norwegianSSN, Nationality.NORWAY),
                successTargetUrl,
                failUrl,
                RedirectCountry.NORWAY
            )
        )

        require(response is UnderwriterStartSignSessionResponse.BankIdRedirect)
        verify { underwriterSignSessionRepository wasNot Called }
        assertThat(response.errorMessages).isNotEmpty
    }

    @Test
    fun dontStartNorwegianBankIdSignIfAlreadySigned() {
        every { signedMemberRepository.findBySsn(norwegianSSN) } returns Optional.of(SignedMemberEntity())

        sut.startSign(
            memberId,
            UnderwriterStartSignSessionRequest.BankIdRedirect(
                underwriterSessionRef,
                NationalIdentification(norwegianSSN, Nationality.NORWAY),
                successTargetUrl,
                failUrl,
                RedirectCountry.NORWAY
            )
        )

        verify { swedishBankIdSigningService wasNot Called }
        verify { underwriterSignSessionRepository wasNot Called }
    }

    @Test
    fun underwriterSignSessionExists_thenShouldBeHandled() {
        every { underwriterSignSessionRepository.findBySignReference(orderRefUUID) } returns UnderwriterSignSessionEntity(underwriterSessionRef, orderRefUUID, null, null)

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
        every {
            signedMemberRepository.findBySsn(any())
        } returns Optional.empty()

        val response = sut.startSign(
            memberId,
            UnderwriterStartSignSessionRequest.BankIdRedirect(
                underwriterSessionRef,
                NationalIdentification(norwegianSSN, Nationality.NORWAY),
                "http://someOther.host",
                "http://someOther.host",
                RedirectCountry.NORWAY
            )
        )

        require(response is UnderwriterStartSignSessionResponse.BankIdRedirect)
        assertThat(response.redirectUrl).isNull()
        assertThat(response.internalErrorMessage).isNotNull()
    }

    @Test
    fun `startDanishBankIdSignSession on already signed member should return internal error message`() {
        every {
            signedMemberRepository.findBySsn(any())
        } returns Optional.of(SignedMemberEntity())
        val response = sut.startSign(
            memberId,
            UnderwriterStartSignSessionRequest.BankIdRedirect(
                underwriterSessionRef,
                NationalIdentification("", Nationality.DENMARK),
                "https://hedvig.com/success",
                "https://hedvig.com/fail",
                RedirectCountry.NORWAY
            )
        )

        require(response is UnderwriterStartSignSessionResponse.BankIdRedirect)
        assertThat(response.redirectUrl).isNull()
        assertThat(response.internalErrorMessage).isNotNull()
    }

    @Test
    fun `startSimpleSignSession on already signed member should return internal error message`() {
        every {
            signedMemberRepository.findBySsn(any())
        } returns Optional.of(SignedMemberEntity())

        val response = sut.startSign(
            123L,
            UnderwriterStartSignSessionRequest.SimpleSign(
                UUID.randomUUID(),
                NationalIdentification("", Nationality.DENMARK)
            )
        )

        require(response is UnderwriterStartSignSessionResponse.SimpleSign)
        assertThat(response.successfullyStarted).isFalse()
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
