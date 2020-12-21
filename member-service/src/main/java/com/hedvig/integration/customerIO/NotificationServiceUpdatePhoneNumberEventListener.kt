package com.hedvig.integration.customerIO

import com.hedvig.integration.notificationService.NotificationService
import com.hedvig.memberservice.events.PhoneNumberUpdatedEvent
import org.axonframework.config.ProcessingGroup
import org.axonframework.eventsourcing.DomainEventMessage
import org.springframework.stereotype.Component

@Component
@ProcessingGroup("NotificationServiceUpdatePhoneNumber")
class NotificationServiceUpdatePhoneNumberEventListener(
    private val notificationService: NotificationService
) {
    fun on(event: PhoneNumberUpdatedEvent, eventMessage: DomainEventMessage<Any>) {
        notificationService.updatePhoneNumber(eventMessage.identifier, event.memberId.toString(), event.phoneNumber)
    }
}
