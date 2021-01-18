package com.hedvig.memberservice.services.customerio

import com.hedvig.integration.notificationService.NotificationService
import com.hedvig.memberservice.events.EmailUpdatedEvent
import com.hedvig.memberservice.events.NameUpdatedEvent
import com.hedvig.memberservice.query.MemberRepository
import com.hedvig.resolver.LocaleResolver
import com.neovisionaries.i18n.CountryCode
import org.axonframework.config.ProcessingGroup
import org.axonframework.eventhandling.EventHandler
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import java.util.Objects

@Component
@Profile("customer.io")
@ProcessingGroup("SegmentProcessorGroup")
class CustomerIOEventListener(
    private val notificationService: NotificationService,
    private val memberRepository: MemberRepository
) {

    @EventHandler
    fun on(evt: NameUpdatedEvent) {
        val traits = mapOf(
            "first_name" to evt.firstName,
            "last_name" to evt.lastName
        )

        sendWithSleep(traits, Objects.toString(evt.memberId))
    }

    @EventHandler
    fun on(evt: EmailUpdatedEvent) {
        val member = memberRepository.findById(evt.memberId).orElseThrow {
            IllegalStateException("No member found for ${evt.memberId}")
        }

        val locale = member.pickedLocale?.locale
            ?: LocaleResolver.resolveNullableLocale(member.acceptLanguage)

        val timeZone = when (val countryCode = CountryCode.getByLocale(locale)) {
            CountryCode.SE -> "Europe/Stockholm"
            CountryCode.NO -> "Europe/Oslo"
            CountryCode.DK -> "Europe/Copenhagen"
            null -> null
            else -> throw RuntimeException("Unsupported country code detected $countryCode")
        }

        val traits = mapOf(
            "email" to evt.email,
            "timezone" to timeZone
        )

        sendWithSleep(traits, Objects.toString(evt.memberId))
    }

    private fun sendWithSleep(traitsMap: Map<String, Any?>, memberId: String) {

        notificationService.updateCustomer(memberId, traitsMap)

        try {
            Thread.sleep(10)
        } catch (e: InterruptedException) {
            throw RuntimeException("Interrupted while throttling segment queueing", e)
        }
    }
}
