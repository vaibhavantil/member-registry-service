package com.hedvig.integration.notificationService

import com.hedvig.integration.notificationService.dto.CancellationEmailSentToInsurerRequest
import com.hedvig.integration.notificationService.dto.EventRequest
import com.hedvig.integration.notificationService.dto.InsuranceActivationDateUpdatedRequest
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader

@FeignClient(name = "notification-service", url = "\${hedvig.notificationservice.baseurl:notification-service}")
interface NotificationServiceClient {
    @PostMapping("/_/notifications/{memberId}/cancellationEmailSentToInsurer")
    fun cancellationEmailSentToInsurer(
        @PathVariable(name = "memberId") memberId: Long?,
        @RequestBody body: CancellationEmailSentToInsurerRequest?
    ): ResponseEntity<*>?

    @PostMapping("/_/notifications/{memberId}/insuranceActivated")
    fun insuranceActivated(@PathVariable(name = "memberId") memberId: Long?): ResponseEntity<*>?

    @PostMapping("/_/notifications/{memberId}/insuranceActivationDateUpdated")
    fun insuranceActivationDateUpdated(
        @PathVariable(name = "memberId") memberId: Long?,
        @RequestBody body: InsuranceActivationDateUpdatedRequest?
    ): ResponseEntity<*>?

    @PostMapping("/_/notifications/insuranceWillBeActivatedAt")
    fun insuranceReminder(@RequestBody NumberOfDaysFromToday: Int): ResponseEntity<*>?

    @DeleteMapping("/_/customerio/{memberId}")
    fun deleteCustomer(@PathVariable memberId: String): ResponseEntity<*>?

    @PostMapping("/_/customerio/{memberId}")
    fun updateCustomer(@PathVariable memberId: String, @RequestBody data: Map<String, Any?>): ResponseEntity<*>?

    @PostMapping("/_/event")
    fun sendEvent(
        @RequestHeader(value = "Request-Id") requestId: String?,
        @RequestBody event: EventRequest?
    ): ResponseEntity<*>?
}
