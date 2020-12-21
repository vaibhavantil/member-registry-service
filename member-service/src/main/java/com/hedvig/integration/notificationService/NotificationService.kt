package com.hedvig.integration.notificationService

import com.hedvig.integration.notificationService.dto.CancellationEmailSentToInsurerRequest
import com.hedvig.integration.notificationService.dto.InsuranceActivationDateUpdatedRequest

interface NotificationService {
    fun cancellationEmailSentToInsurer(memberId: Long, body: CancellationEmailSentToInsurerRequest)
    fun insuranceActivated(memberId: Long)
    fun insuranceActivationDateUpdated(memberId: Long, body: InsuranceActivationDateUpdatedRequest)
    fun insuranceReminder(NumberOfDaysFromToday: Int)
    fun deleteCustomer(memberId: String)
    fun updateCustomer(memberId: String, traitsMap: Map<String, Any?>)
    fun updatePhoneNumber(eventId: String, memberId: String, phoneNumber: String)
}
