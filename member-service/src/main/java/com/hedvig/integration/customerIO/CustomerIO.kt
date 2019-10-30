package com.hedvig.integration.customerIO

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.cloud.openfeign.EnableFeignClients
import org.springframework.stereotype.Component

@ConditionalOnProperty(value = ["customerio.siteId", "customerio.apiKey"], matchIfMissing = false)
@Component
@EnableFeignClients
class CustomerIO(
    private val customerIOClient: CustomerIOClient
){
    fun deleteUser(userId: String) {
        this.customerIOClient.delete(userId)
    }
}
