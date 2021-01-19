package com.hedvig.memberservice.services.customerio

import com.hedvig.integration.notificationService.NotificationService
import com.hedvig.memberservice.events.EmailUpdatedEvent
import com.hedvig.memberservice.events.NameUpdatedEvent
import com.hedvig.memberservice.query.MemberEntity
import com.hedvig.memberservice.query.MemberRepository
import com.hedvig.memberservice.services.trustpilot.TrustpilotReviewService
import com.hedvig.resolver.LocaleResolver
import org.axonframework.config.ProcessingGroup
import org.axonframework.eventhandling.EventHandler
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import java.util.Locale
import java.util.Objects

@Component
@Profile("customer.io")
@ProcessingGroup("CustomerIOTrustpilot")
class CustomerIOTrustpilotEventListener(
    private val notificationService: NotificationService,
    private val memberRepository: MemberRepository,
    private val trustpilotReviewService: TrustpilotReviewService
) {

    @EventHandler
    fun on(event: NameUpdatedEvent) {
        val member = memberRepository.findById(event.memberId).orElseThrow {
            IllegalStateException("No member found when generating Trustpilot link (memberId=${event.memberId})")
        }

        val attributes = createTrustpilotReviewAttributes(
            member.id,
            getLocaleForMember(member),
            member.email,
            event.firstName,
            event.lastName
        )

        if (attributes.isNotEmpty()) {
            sendWithSleep(attributes, Objects.toString(event.memberId))
        }
    }

    @EventHandler
    fun on(evt: EmailUpdatedEvent) {
        val member = memberRepository.findById(evt.memberId).orElseThrow {
            IllegalStateException("No member found for ${evt.memberId}")
        }

        val attributes = createTrustpilotReviewAttributes(
            member.id,
            getLocaleForMember(member),
            evt.email,
            member.firstName,
            member.lastName
        )

        if (attributes.isNotEmpty()) {
            sendWithSleep(attributes, Objects.toString(evt.memberId))
        }
    }

    private fun getLocaleForMember(member: MemberEntity): Locale? {
        return member.pickedLocale?.locale
            ?: LocaleResolver.resolveNullableLocale(member.acceptLanguage)
    }

    private fun createTrustpilotReviewAttributes(
        memberId: Long,
        locale: Locale?,
        email: String?,
        firstName: String?,
        lastName: String?
    ): Map<String, String> {
        firstName ?: return emptyMap()
        lastName ?: return emptyMap()
        email ?: return emptyMap()

        val invitation = trustpilotReviewService.generateTrustpilotReviewInvitation(
            memberId, email, "${firstName.capitalize()} ${lastName.capitalize()}", locale
        )
        invitation ?: return emptyMap()

        return mapOf(
            "trustpilot_review_link" to invitation.url,
            "trustpilot_review_id" to invitation.id
        )
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
