package com.hedvig.memberservice.services.segmentpublisher

import com.google.common.collect.ImmutableMap
import com.hedvig.integration.notificationService.NotificationService
import com.hedvig.memberservice.events.EmailUpdatedEvent
import com.hedvig.memberservice.events.NameUpdatedEvent
import org.axonframework.config.ProcessingGroup
import org.axonframework.eventhandling.EventHandler
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import java.util.Objects

@Component
@Profile("customer.io")
@ProcessingGroup("SegmentProcessorGroup")
class EventListener @Autowired constructor(
    private val notificationService: NotificationService
) {

    @EventHandler
    fun on(evt: NameUpdatedEvent) {
        val traits = ImmutableMap
            .of<String, Any>("first_name", evt.firstName, "last_name", evt.lastName)
        segmentEnqueue(traits, Objects.toString(evt.memberId))
    }

    @EventHandler
    fun on(evt: EmailUpdatedEvent) {
        val traits = ImmutableMap
            .of<String, Any>(
                "email", evt.email,
                "timezone", "Europe/Stockholm "
            )
        segmentEnqueue(traits, Objects.toString(evt.id))
    }

    private fun segmentEnqueue(traitsMap: Map<String, Any>, memberId: String) {

        notificationService.updateCustomer(memberId, traitsMap)

        try {
            Thread.sleep(10)
        } catch (e: InterruptedException) {
            throw RuntimeException("Interrupted while throttling segment queueing", e)
        }
    }
}
