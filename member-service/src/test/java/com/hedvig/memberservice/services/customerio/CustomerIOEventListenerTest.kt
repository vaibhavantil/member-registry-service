package com.hedvig.memberservice.services.customerio

import com.hedvig.integration.notificationService.NotificationService
import com.hedvig.memberservice.aggregates.PickedLocale
import com.hedvig.memberservice.events.EmailUpdatedEvent
import com.hedvig.memberservice.external.trustpilot.TrustpilotReviewLinkResponseDto
import com.hedvig.memberservice.query.MemberEntity
import com.hedvig.memberservice.query.MemberRepository
import com.hedvig.memberservice.services.trustpilot.TrustpilotReviewService
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.slot
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

import org.junit.Before
import java.lang.IllegalStateException
import java.util.Optional

class CustomerIOEventListenerTest {

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
    fun swedish_picked_locale_sets_timezone_to_stockholm() {

        val member = MemberEntity()
        member.pickedLocale = PickedLocale.en_SE
        member.id = 123
        every { memberRepository.findById(any()) } returns Optional.of(member)

        every { trustpilotReviewService.generateTrustpilotReviewLinkForMember(any()) } returns
            TrustpilotReviewLinkResponseDto("id", "url")

        val sut = CustomerIOEventListener(notificationService, memberRepository, trustpilotReviewService)

        sut.on(EmailUpdatedEvent(1337L, "omse@lkj.com"))

        val slot = slot<Map<String, Any?>>()
        verify { notificationService.updateCustomer(any(), capture(slot)) }

        assertThat(slot.captured["timezone"]).isEqualTo("Europe/Stockholm")
    }

    @Test
    fun norwegian_picked_locale_sets_timezone_to_oslo() {

        val member = MemberEntity()
        member.id = 123
        member.pickedLocale = PickedLocale.nb_NO
        every { memberRepository.findById(any()) } returns Optional.of(member)

        every { trustpilotReviewService.generateTrustpilotReviewLinkForMember(any()) } returns
            TrustpilotReviewLinkResponseDto("id", "url")

        val sut = CustomerIOEventListener(notificationService, memberRepository, trustpilotReviewService)

        sut.on(EmailUpdatedEvent(1337L, "omse@lkj.com"))

        val slot = slot<Map<String, Any?>>()
        verify { notificationService.updateCustomer(any(), capture(slot)) }

        assertThat(slot.captured["timezone"]).isEqualTo("Europe/Oslo")
    }

    @Test
    fun no_picked_locale_sets_timezone_to_null() {

        val member = MemberEntity()
        member.id = "123".toLong()
        member.pickedLocale = null
        every { memberRepository.findById(any()) } returns Optional.of(member)

        val sut = CustomerIOEventListener(notificationService, memberRepository, trustpilotReviewService)

        every { trustpilotReviewService.generateTrustpilotReviewLinkForMember(any()) } returns
            TrustpilotReviewLinkResponseDto("id", "url")

        sut.on(EmailUpdatedEvent(1337L, "omse@lkj.com"))

        val slot = slot<Map<String, Any?>>()
        verify { notificationService.updateCustomer(any(), capture(slot)) }

        assertThat(slot.captured["timezone"]).isNull()
    }

    @Test(expected = IllegalStateException::class)
    fun no_memberentity_found_fails() {

        every { memberRepository.findById(any()) } returns Optional.empty()

        val sut = CustomerIOEventListener(notificationService, memberRepository, trustpilotReviewService)

        every { trustpilotReviewService.generateTrustpilotReviewLinkForMember(any()) } returns
            TrustpilotReviewLinkResponseDto("id", "url")

        sut.on(EmailUpdatedEvent(1337L, "omse@lkj.com"))
    }
}
