package com.hedvig.memberservice.services.customerio

import com.google.common.collect.ImmutableMap
import com.hedvig.integration.notificationService.NotificationService
import com.hedvig.memberservice.events.EmailUpdatedEvent
import com.hedvig.memberservice.events.NameUpdatedEvent
import com.hedvig.memberservice.query.MemberRepository
import com.neovisionaries.i18n.CountryCode
import org.axonframework.config.ProcessingGroup
import org.axonframework.eventhandling.EventHandler
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import java.util.Objects

@Component
@Profile("customer.io")
@ProcessingGroup("SegmentProcessorGroup")
class EventListener @Autowired constructor(
    private val notificationService: NotificationService,
    private val memberRepository: MemberRepository
) {
    private val logger: Logger = LoggerFactory.getLogger(EventListener::class.java)

    @EventHandler
    fun on(evt: NameUpdatedEvent) {
        val traits = ImmutableMap
            .of<String, Any>("first_name", evt.firstName, "last_name", evt.lastName)
        sendWithSleep(traits, Objects.toString(evt.memberId))
    }

    @EventHandler
    fun on(evt: EmailUpdatedEvent) {
        val member = memberRepository.findById(evt.memberId)

        if(member.isPresent) {

            val timeZone = when (val countryCode = CountryCode.getByLocale(member.get().pickedLocale?.locale)) {
                CountryCode.SE -> "Europe/Stockholm"
                CountryCode.NO -> "Europe/Oslo"
                null -> "Europe/Stockholm"
                else -> RuntimeException("Unsupported country code detected $countryCode")
            }

            val traits = ImmutableMap
                .of<String, Any>(
                    "email", evt.email,
                    "timezone", timeZone
                )
            sendWithSleep(traits, Objects.toString(evt.memberId))
        } else {
            logger.error("Could not update email for member ${evt.memberId} member not found in MemberRepository")
        }
    }

    private fun sendWithSleep(traitsMap: Map<String, Any>, memberId: String) {

        notificationService.updateCustomer(memberId, traitsMap)

        try {
            Thread.sleep(10)
        } catch (e: InterruptedException) {
            throw RuntimeException("Interrupted while throttling segment queueing", e)
        }
    }
}
