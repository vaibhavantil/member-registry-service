package com.hedvig.memberservice.services.signing.underwriter

import com.hedvig.integration.underwriter.UnderwriterClient
import com.hedvig.memberservice.query.SignedMemberEntity
import com.hedvig.memberservice.query.SignedMemberRepository
import com.hedvig.memberservice.query.UnderwriterSignSessionRepository
import com.hedvig.memberservice.services.signing.simple.SimpleSigningService
import com.hedvig.memberservice.services.signing.sweden.SwedishBankIdSigningService
import com.hedvig.memberservice.services.signing.zignsec.ZignSecSigningService
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.AssertionsForClassTypes.assertThat
import org.junit.jupiter.api.Test
import java.util.Optional
import java.util.UUID

class UnderwriterSigningServiceImplTest {

    private val underwriterSignSessionRepository: UnderwriterSignSessionRepository = mockk()
    private val underwriterClient: UnderwriterClient = mockk()
    private val swedishBankIdSigningService: SwedishBankIdSigningService = mockk()
    private val zignSecSigningService: ZignSecSigningService = mockk()
    private val simpleSigningService: SimpleSigningService = mockk()
    private val signedMemberRepository: SignedMemberRepository = mockk()

    private val cut = UnderwriterSigningServiceImpl(
        underwriterSignSessionRepository,
        underwriterClient,
        swedishBankIdSigningService,
        zignSecSigningService,
        simpleSigningService,
        signedMemberRepository,
        arrayOf("hedvig.com")
    )

    @Test
    fun `startSwedishBankIdSignSession on already signed member should return internal error message`() {
        every {
            signedMemberRepository.findBySsn(any())
        } returns Optional.of(SignedMemberEntity())

        val response = cut.startSwedishBankIdSignSession(UUID.randomUUID(), 123L, "", "", true)

        assertThat(response.autoStartToken).isNull()
        assertThat(response.internalErrorMessage).isNotNull()
    }

    @Test
    fun `startNorwegianBankIdSignSession on already signed member should return internal error message`() {
        every {
            signedMemberRepository.findBySsn(any())
        } returns Optional.of(SignedMemberEntity())

        val response = cut.startNorwegianBankIdSignSession(UUID.randomUUID(), 123L, "", "https://hedvig.com/success", "https://hedvig.com/fail")

        assertThat(response.redirectUrl).isNull()
        assertThat(response.internalErrorMessage).isNotNull()
    }

    @Test
    fun `startDanishBankIdSignSession on already signed member should return internal error message`() {
        every {
            signedMemberRepository.findBySsn(any())
        } returns Optional.of(SignedMemberEntity())

        val response = cut.startDanishBankIdSignSession(UUID.randomUUID(), 123L, "", "https://hedvig.com/success", "https://hedvig.com/fail")

        assertThat(response.redirectUrl).isNull()
        assertThat(response.internalErrorMessage).isNotNull()
    }

    @Test
    fun `startSimpleSignSession on already signed member should return internal error message`() {
        every {
            signedMemberRepository.findBySsn(any())
        } returns Optional.of(SignedMemberEntity())

        val response = cut.startSimpleSignSession(UUID.randomUUID(), 123L, "")

        assertThat(response.successfullyStarted).isFalse()
        assertThat(response.internalErrorMessage).isNotNull()
    }
}
