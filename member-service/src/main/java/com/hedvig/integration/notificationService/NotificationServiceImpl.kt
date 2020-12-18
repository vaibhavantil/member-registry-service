package com.hedvig.integration.notificationService

import com.hedvig.integration.notificationService.dto.CancellationEmailSentToInsurerRequest
import com.hedvig.integration.notificationService.dto.InsuranceActivationDateUpdatedRequest
import com.hedvig.integration.notificationService.dto.PhoneNumberUpdatedEvent
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class NotificationServiceImpl(private val notificationServiceClient: NotificationServiceClient) : NotificationService {
    override fun cancellationEmailSentToInsurer(
        memberId: Long,
        request: CancellationEmailSentToInsurerRequest) {
        notificationServiceClient.cancellationEmailSentToInsurer(memberId, request)
    }

    override fun insuranceActivated(memberId: Long) {
        notificationServiceClient.insuranceActivated(memberId)
    }

    override fun insuranceActivationDateUpdated(
        memberId: Long, request: InsuranceActivationDateUpdatedRequest) {
        notificationServiceClient.insuranceActivationDateUpdated(memberId, request)
    }

    override fun insuranceReminder(NumberOfDaysFromToday: Int) {
        notificationServiceClient.insuranceReminder(NumberOfDaysFromToday)
    }

    override fun deleteCustomer(memberId: String) {
        notificationServiceClient.deleteCustomer(memberId)
    }

    override fun updateCustomer(memberId: String, data: Map<String, Any?>) {
        notificationServiceClient.updateCustomer(memberId, data)
    }

    override fun updatePhoneNumber(eventId: String, memberId: String, phoneNumber: String) {
        notificationServiceClient.sendEvent(eventId, PhoneNumberUpdatedEvent(memberId, phoneNumber))
    }
}
