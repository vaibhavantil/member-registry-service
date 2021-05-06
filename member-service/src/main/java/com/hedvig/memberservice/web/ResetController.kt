package com.hedvig.memberservice.web

import java.time.Instant
import org.axonframework.config.EventProcessingConfiguration
import org.axonframework.eventhandling.TrackingEventProcessor
import org.axonframework.messaging.StreamableMessageSource
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
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

    @PutMapping("/CustomerIOTrustpilot")
    fun resetCustomerIOTrustpilotSegmentProcessorGroup() {
        eventProcessingConfiguration
            .eventProcessorByProcessingGroup("CustomerIOTrustpilot", TrackingEventProcessor::class.java)
            .ifPresent { trackingEventProcessor ->
                trackingEventProcessor.shutDown()
                trackingEventProcessor.resetTokens()
                trackingEventProcessor.start()
            }
    }

    @PutMapping("/MemberToUserExport")
    fun resetMemberToUserExporters(
        @RequestParam fromTime: Instant
    ) {
        eventProcessingConfiguration
            .eventProcessorByProcessingGroup("com.hedvig.memberservice.users", TrackingEventProcessor::class.java)
            .ifPresent { trackingEventProcessor ->
                trackingEventProcessor.shutDown()
                trackingEventProcessor.resetTokens {
                    it.createTokenAt(fromTime)
                }
                trackingEventProcessor.start()
            }
    }
}
