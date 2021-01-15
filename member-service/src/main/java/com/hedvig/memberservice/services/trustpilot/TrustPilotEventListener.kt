package com.hedvig.memberservice.services.trustpilot

import com.hedvig.memberservice.events.EmailUpdatedEvent
import org.axonframework.eventhandling.EventHandler
import org.springframework.stereotype.Component

@Component
class TrustPilotEventListener {
    @EventHandler
    fun on(event: EmailUpdatedEvent) {

    }
}
