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
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExpectedException
import java.util.Optional

class CustomerIOEventListenerTest {

    @MockK(relaxed = true)
    lateinit var notificationService: NotificationService

    @MockK
    lateinit var memberRepository: MemberRepository

    @Before
    fun setup(){
        MockKAnnotations.init(this)
    }

    @Test
    fun `swedish picked locale sets timezone to stockholm`() {

        val member = MemberEntity()
        member.pickedLocale = PickedLocale.en_SE
        member.id = 123
        every { memberRepository.findById(any()) } returns Optional.of(member)

        val sut = CustomerIOEventListener(notificationService, memberRepository)

        sut.on(EmailUpdatedEvent(1337L, "omse@lkj.com"))

        val slot = slot<Map<String, Any?>>()
        verify { notificationService.updateCustomer(any(), capture(slot)) }

        assertThat(slot.captured["timezone"]).isEqualTo("Europe/Stockholm")
    }

    @Test
    fun `norwegian picked locale sets timezone to oslo`() {

        val member = MemberEntity()
        member.id = 123
        member.pickedLocale = PickedLocale.nb_NO
        every { memberRepository.findById(any()) } returns Optional.of(member)

        val sut = CustomerIOEventListener(notificationService, memberRepository)

        sut.on(EmailUpdatedEvent(1337L, "omse@lkj.com"))

        val slot = slot<Map<String, Any?>>()
        verify { notificationService.updateCustomer(any(), capture(slot)) }

        assertThat(slot.captured["timezone"]).isEqualTo("Europe/Oslo")
    }

    @Test
    fun `no picked locale sets timezone to null`() {
        val member = MemberEntity()
        member.id = "123".toLong()
        member.pickedLocale = null
        every { memberRepository.findById(any()) } returns Optional.of(member)

        val sut = CustomerIOEventListener(notificationService, memberRepository)

        sut.on(EmailUpdatedEvent(1337L, "omse@lkj.com"))

        val slot = slot<Map<String, Any?>>()
        verify { notificationService.updateCustomer(any(), capture(slot)) }

        assertThat(slot.captured["timezone"]).isNull()
    }

    @get:Rule
    val thrown = ExpectedException.none()

    @Test
    fun `no memberentity found fails`() {
        every { memberRepository.findById(any()) } returns Optional.empty()

        val sut = CustomerIOEventListener(notificationService, memberRepository)

        thrown.expect(IllegalStateException::class.java)
        sut.on(EmailUpdatedEvent(1337L, "omse@lkj.com"))
    }
}
