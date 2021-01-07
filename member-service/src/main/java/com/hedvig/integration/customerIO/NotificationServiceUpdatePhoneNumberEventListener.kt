package com.hedvig.integration.customerIO

import com.hedvig.integration.notificationService.NotificationService
import com.hedvig.memberservice.events.PhoneNumberUpdatedEvent
import com.hedvig.memberservice.util.logger
import feign.FeignException
import org.axonframework.config.ProcessingGroup
import org.axonframework.eventhandling.EventHandler
import org.axonframework.eventsourcing.DomainEventMessage
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
@ProcessingGroup("NotificationServiceUpdatePhoneNumber")
class NotificationServiceUpdatePhoneNumberEventListener(
    private val notificationService: NotificationService
) {

    @EventHandler
    fun on(event: PhoneNumberUpdatedEvent, eventMessage: DomainEventMessage<Any>) {
        logger.info("Updating notification service phone number with [event: $event]")
        try {
            notificationService.updatePhoneNumber(eventMessage.identifier, event.memberId.toString(), event.phoneNumber)
        } catch (e: RuntimeException) {
            logger.error("Notification service phone number update failed to handle event: $event")
        }
    }
}
