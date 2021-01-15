package com.hedvig.memberservice.services.customerio

import com.google.common.collect.ImmutableMap
import com.hedvig.integration.notificationService.NotificationService
import com.hedvig.memberservice.events.EmailUpdatedEvent
import com.hedvig.memberservice.events.NameUpdatedEvent
import com.hedvig.memberservice.external.trustpilot.TrustpilotReviewLinkRequestDto
import com.hedvig.memberservice.external.trustpilot.TrustpilotReviewLinkResponseDto
import com.hedvig.memberservice.query.MemberEntity
import com.hedvig.memberservice.query.MemberRepository
import com.hedvig.memberservice.services.trustpilot.TrustpilotReviewInvitation
import com.hedvig.memberservice.services.trustpilot.TrustpilotReviewService
import com.hedvig.resolver.LocaleResolver
import com.neovisionaries.i18n.CountryCode
import org.axonframework.config.ProcessingGroup
import org.axonframework.eventhandling.EventHandler
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import java.util.Locale
import java.util.Objects

@Component
@Profile("customer.io")
@ProcessingGroup("SegmentProcessorGroup")
class CustomerIOEventListener @Autowired constructor(
    private val notificationService: NotificationService,
    private val memberRepository: MemberRepository,
    private val trustpilotReviewService: TrustpilotReviewService
) {

    @EventHandler
    fun on(evt: NameUpdatedEvent) {
        val member = memberRepository.findById(evt.memberId).orElseThrow {
            IllegalStateException("No member found for ${evt.memberId}")
        }

        val locale = getLocaleForMember(member)

        val trustPilotLinkResponse = getTrustpilotLink(
            member.id,
            locale,
            member.email,
            evt.firstName,
            evt.lastName
        )

        val traits = mutableMapOf(
            "first_name" to evt.firstName,
            "last_name" to evt.lastName
        )

        if (trustPilotLinkResponse != null) {
            traits["trustpilotLink"] = trustPilotLinkResponse.url
        }
        sendWithSleep(traits, Objects.toString(evt.memberId))
    }

    @EventHandler
    fun on(evt: EmailUpdatedEvent) {
        val member = memberRepository.findById(evt.memberId).orElseThrow {
            IllegalStateException("No member found for ${evt.memberId}")
        }

        val locale = getLocaleForMember(member)

        val timeZone = when (val countryCode = CountryCode.getByLocale(locale)) {
            CountryCode.SE -> "Europe/Stockholm"
            CountryCode.NO -> "Europe/Oslo"
            CountryCode.DK -> "Europe/Copenhagen"
            null -> null
            else -> throw RuntimeException("Unsupported country code detected $countryCode")
        }

//            put this in a try?
        val trustPilotLinkResponse = getTrustpilotLink(
            member.id,
            locale,
            evt.email,
            member.firstName,
            member.lastName
        )

        val traits = mutableMapOf(
            "email" to evt.email,
            "timezone" to timeZone
        )

        if (trustPilotLinkResponse != null) {
            traits["trustpilotLink"] = trustPilotLinkResponse.url
        }
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

    private fun getLocaleForMember(member: MemberEntity): Locale? {
        return member.pickedLocale?.locale
            ?: LocaleResolver.resolveNullableLocale(member.acceptLanguage)
    }

    private fun getTrustpilotLink(
        memberId: Long, locale: Locale?, email: String?, firstName: String?, lastName: String?
    ): TrustpilotReviewInvitation? {
        firstName ?: return null
        lastName ?: return null
        email ?: return null

        return trustpilotReviewService.generateTrustpilotReviewInvitation(
            memberId, email, "${firstName.capitalize()} ${lastName.capitalize()}", locale
        )
    }
}
