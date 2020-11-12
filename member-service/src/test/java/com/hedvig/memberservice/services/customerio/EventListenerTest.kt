package com.hedvig.memberservice.services.customerio

import com.hedvig.integration.notificationService.NotificationService
import com.hedvig.memberservice.aggregates.PickedLocale
import com.hedvig.memberservice.events.EmailUpdatedEvent
import com.hedvig.memberservice.query.MemberEntity
import com.hedvig.memberservice.query.MemberRepository
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.slot
import io.mockk.verify
import org.junit.Test

import org.junit.Assert.*
import org.junit.Before
import java.util.Optional

class EventListenerTest {

    @MockK(relaxed = true)
    lateinit var notificationService: NotificationService

    @MockK
    lateinit var memberRepository: MemberRepository


    @Before
    fun setup(){
        MockKAnnotations.init(this)
    }

    @Test
    fun swedish_picked_locale_sets_timezone_to_sockholm() {

        val member = MemberEntity()
        member.pickedLocale = PickedLocale.en_SE
        every { memberRepository.findById(any()) } returns Optional.of(member)


        val sut = EventListener(notificationService, memberRepository)

        sut.on(EmailUpdatedEvent(1337L, "omse@lkj.com"))

        val slot = slot<Map<String, Any?>>()
        verify { notificationService.updateCustomer(any(), capture(slot)) }

        assert(slot.captured["timezone"] == "Europe/Stockholm")
    }

    @Test
    fun norwegian_picked_locale_sets_timezone_to_oslo() {

        val member = MemberEntity()
        member.pickedLocale = PickedLocale.nb_NO
        every { memberRepository.findById(any()) } returns Optional.of(member)

        val sut = EventListener(notificationService, memberRepository)

        sut.on(EmailUpdatedEvent(1337L, "omse@lkj.com"))

        val slot = slot<Map<String, Any?>>()
        verify { notificationService.updateCustomer(any(), capture(slot)) }

        assert(slot.captured["timezone"] == "Europe/Oslo")
    }

    @Test
    fun no_picked_locale_sets_timezone_to_null() {

        val member = MemberEntity()
        member.pickedLocale = null
        every { memberRepository.findById(any()) } returns Optional.of(member)

        val sut = EventListener(notificationService, memberRepository)

        sut.on(EmailUpdatedEvent(1337L, "omse@lkj.com"))

        val slot = slot<Map<String, Any?>>()
        verify { notificationService.updateCustomer(any(), capture(slot)) }

        assert(slot.captured["timezone"] == null)
    }

    @Test
    fun no_memberentity_found_does_nothing() {

        every { memberRepository.findById(any()) } returns Optional.empty()

        val sut = EventListener(notificationService, memberRepository)

        sut.on(EmailUpdatedEvent(1337L, "omse@lkj.com"))

        verify(inverse = true) { notificationService.updateCustomer(any(), any()) }

    }
}
