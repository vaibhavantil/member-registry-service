package com.hedvig.memberservice.web

import org.axonframework.config.EventProcessingConfiguration
import org.axonframework.eventhandling.TrackingEventProcessor
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RequestMapping("/_/dangerously/reset")
@RestController
class ResetController(
    private val eventProcessingConfiguration: EventProcessingConfiguration
) {
    @DeleteMapping("/CleanCustomerIO")
    fun resetCleanCustomerIO() {
        eventProcessingConfiguration
            .eventProcessorByProcessingGroup("CleanCustomerIO", TrackingEventProcessor::class.java)
            .ifPresent { trackingEventProcessor ->
                trackingEventProcessor.shutDown()
                trackingEventProcessor.resetTokens()
                trackingEventProcessor.start()
            }
    }
}
