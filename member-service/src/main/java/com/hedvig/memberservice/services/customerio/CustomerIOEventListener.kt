package com.hedvig.memberservice.services.customerio

import com.hedvig.integration.notificationService.NotificationService
import com.hedvig.memberservice.events.EmailUpdatedEvent
import com.hedvig.memberservice.events.NameUpdatedEvent
import com.hedvig.memberservice.query.MemberEntity
import com.hedvig.memberservice.query.MemberRepository
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

        val traits = mutableMapOf(
            "first_name" to evt.firstName,
            "last_name" to evt.lastName
        )

        attachTrustpilotReviewInvitation(
            member.id,
            locale,
            member.email,
            evt.firstName,
            evt.lastName,
            traits
        )

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

        val traits = mutableMapOf(
            "email" to evt.email,
            "timezone" to timeZone
        )

        attachTrustpilotReviewInvitation(
            member.id,
            locale,
            evt.email,
            member.firstName,
            member.lastName,
            traits
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

    private fun getLocaleForMember(member: MemberEntity): Locale? {
        return member.pickedLocale?.locale
            ?: LocaleResolver.resolveNullableLocale(member.acceptLanguage)
    }

    private fun attachTrustpilotReviewInvitation(
        memberId: Long, locale: Locale?, email: String?, firstName: String?, lastName: String?,
        traits: MutableMap<String, String?>
    ) {
        firstName ?: return
        lastName ?: return
        email ?: return

        val invitation = trustpilotReviewService.generateTrustpilotReviewInvitation(
            memberId, email, "${firstName.capitalize()} ${lastName.capitalize()}", locale
        )

        invitation?.let {
            traits["trustpilot_review_link"] = invitation.url
            traits["trustpilot_review_id"] = invitation.id
        }
    }
}
