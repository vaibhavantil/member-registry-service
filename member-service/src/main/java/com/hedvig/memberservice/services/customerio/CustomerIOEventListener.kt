package com.hedvig.memberservice.services.customerio

import com.google.common.collect.ImmutableMap
import com.hedvig.integration.notificationService.NotificationService
import com.hedvig.memberservice.events.EmailUpdatedEvent
import com.hedvig.memberservice.events.NameUpdatedEvent
import com.hedvig.memberservice.external.trustpilot.TrustpilotReviewLinkRequestDto
import com.hedvig.memberservice.external.trustpilot.TrustpilotReviewLinkResponseDto
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
        val member = getMemberOrThrowError(evt.memberId)

        val locale = getLocaleForMember(member)

        val trustPilotLinkResponse = getTrustpilotLink(
            member.id.toString(),
            locale,
            member.email,
            evt.firstName,
            evt.lastName
        )

        val traits = ImmutableMap.of<String, Any>(
                "first_name", evt.firstName,
                "last_name", evt.lastName
            )

        if (trustPilotLinkResponse != null) {
            traits.plus("trustpilotLink" to  "trustPilotLinkResponse")
        }
        sendWithSleep(traits, Objects.toString(evt.memberId))
    }

    @EventHandler
    fun on(evt: EmailUpdatedEvent) {
        val member = getMemberOrThrowError(evt.memberId)

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
            member.id.toString(),
            locale,
            evt.email,
            member.firstName,
            member.lastName
        )

        val traits = mapOf(
            "email" to evt.email,
            "timezone" to timeZone
        )

        if (trustPilotLinkResponse != null) {
            traits.plus("trustpilotLink" to  "trustPilotLinkResponse")
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
//            ?: Locale("sv", "SE")
    }

    private fun getMemberOrThrowError(memberId: Long): MemberEntity {
        val member = memberRepository.findById(memberId)

        if (!member.isPresent) {
            throw error("Could not update email for member $memberId member not found in MemberRepository")
        }
        return member.get()
    }

    private fun getTrustpilotLink(
        memberId: String, locale: Locale, email: String?, firstName: String?, lastName: String?
    ): TrustpilotReviewLinkResponseDto? {

        val trustpilotReviewLinkRequest =  when {
            email != null && firstName != null && lastName != null -> TrustpilotReviewLinkRequestDto.from(
                memberId,
                locale,
                email,
                "${firstName.capitalize()} ${lastName.capitalize()}"
            )
            else -> null
        }

        return if (trustpilotReviewLinkRequest != null) {
            trustpilotReviewService.generateTrustpilotReviewLinkForMember(trustpilotReviewLinkRequest)
        }
        else {
            null
        }
    }
}
