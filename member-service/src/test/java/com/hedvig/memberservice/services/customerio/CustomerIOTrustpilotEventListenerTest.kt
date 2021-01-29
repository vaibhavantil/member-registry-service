package com.hedvig.memberservice.services.customerio

import com.hedvig.integration.notificationService.NotificationService
import com.hedvig.memberservice.aggregates.PickedLocale
import com.hedvig.memberservice.events.EmailUpdatedEvent
import com.hedvig.memberservice.query.MemberEntity
import com.hedvig.memberservice.query.MemberRepository
import com.hedvig.memberservice.services.trustpilot.CustomerIOTrustpilotEventListener
import com.hedvig.memberservice.services.trustpilot.TrustpilotReviewInvitation
import com.hedvig.memberservice.services.trustpilot.TrustpilotReviewService
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.slot
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

import org.junit.Before
import org.junit.Rule
import org.junit.rules.ExpectedException
import java.lang.IllegalStateException
import java.util.Optional

class CustomerIOTrustpilotEventListenerTest {

    @MockK(relaxed = true)
    lateinit var notificationService: NotificationService

    @MockK
    lateinit var memberRepository: MemberRepository

    @MockK
    lateinit var trustpilotReviewService: TrustpilotReviewService

    @Before
    fun setup(){
        MockKAnnotations.init(this)
    }

    @Test
    fun `trustpilot link is set if created successfully`() {
        val member = MemberEntity()
        member.pickedLocale = PickedLocale.en_SE
        member.id = 123
        member.firstName = "Example"
        member.lastName = "Person"
        every { memberRepository.findById(any()) } returns Optional.of(member)

        every {
            trustpilotReviewService.generateTrustpilotReviewInvitation(any(), any(), any(), any())
        } returns TrustpilotReviewInvitation("expected-id", "expected-url")

        val sut = CustomerIOTrustpilotEventListener(notificationService, memberRepository, trustpilotReviewService)

        sut.on(EmailUpdatedEvent(1337L, "omse@lkj.com"))

        val slot = slot<Map<String, Any?>>()
        verify { notificationService.updateCustomer(any(), capture(slot)) }

        assertThat(slot.captured["trustpilot_review_link"]).isEqualTo("expected-url")
        assertThat(slot.captured["trustpilot_review_id"]).isEqualTo("expected-id")
    }

    @Test
    fun `if trustpilot returns null, don't call notification service`() {
        val member = MemberEntity()
        member.pickedLocale = PickedLocale.en_SE
        member.id = 123
        every { memberRepository.findById(any()) } returns Optional.of(member)

        every {
            trustpilotReviewService.generateTrustpilotReviewInvitation(any(), any(), any(), any())
        } returns null

        val sut = CustomerIOTrustpilotEventListener(notificationService, memberRepository, trustpilotReviewService)

        sut.on(EmailUpdatedEvent(1337L, "omse@lkj.com"))

        verify(inverse = true) {
            notificationService.updateCustomer(any(), any())
        }
    }

    @get:Rule
    val thrown = ExpectedException.none()

    @Test
    fun `no memberentity found fails`() {
        every { memberRepository.findById(any()) } returns Optional.empty()

        val sut = CustomerIOTrustpilotEventListener(notificationService, memberRepository, trustpilotReviewService)

        thrown.expect(IllegalStateException::class.java)
        sut.on(EmailUpdatedEvent(1337L, "omse@lkj.com"))
    }
}
