package com.hedvig.memberservice.services.trustpilot

import com.hedvig.memberservice.external.trustpilot.TrustpilotClient
import com.hedvig.memberservice.external.trustpilot.TrustpilotReviewLinkRequestDto
import com.hedvig.memberservice.external.trustpilot.TrustpilotReviewLinkResponseDto
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.slot
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import java.lang.RuntimeException
import java.util.Locale

internal class TrustpilotReviewServiceImplTest {

    @MockK
    lateinit var client: TrustpilotClient

    @Before
    fun setUp() = MockKAnnotations.init(this)

    @Test
    fun `response is propagated accordingly`() {

        every {
            client.createReviewLink("buid", any())
        } returns TrustpilotReviewLinkResponseDto("invite-id", "invite-url")

        val sut = TrustpilotReviewServiceImpl(client)

        val output = sut.generateTrustpilotReviewInvitation(
            123, "email@hedvig.com", "Person", Locale.UK
        )

        assertThat(output?.id).isEqualTo("invite-id")
        assertThat(output?.url).isEqualTo("invite-url")
    }

    @Test
    fun `trustpilot API failures returns null`() {

        every {
            client.createReviewLink("buid", any())
        } throws RuntimeException()

        val sut = TrustpilotReviewServiceImpl(client)

        val output = sut.generateTrustpilotReviewInvitation(
            123, "email@hedvig.com", "Person", Locale.UK
        )

        assertThat(output).isNull()
    }

    @Test
    fun `null locales default to default locale`() {

        val slot = slot<TrustpilotReviewLinkRequestDto>()
        every {
            client.createReviewLink("buid", capture(slot))
        } returns TrustpilotReviewLinkResponseDto("invite-id", "invite-url")

        val sut = TrustpilotReviewServiceImpl(client)

        sut.generateTrustpilotReviewInvitation(
            123, "email@hedvig.com", "Person", null
        )

        assertThat(slot.captured.locale).isEqualTo("sv-SE")
    }
}
