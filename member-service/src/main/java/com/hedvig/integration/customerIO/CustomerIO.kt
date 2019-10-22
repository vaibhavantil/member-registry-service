package com.hedvig.integration.customerIO

import org.springframework.cloud.openfeign.EnableFeignClients
import org.springframework.stereotype.Component

@Component
@EnableFeignClients
class CustomerIO(
    private val customerIOClient: CustomerIOClient
){
    fun deleteUser(userId: String) {
        this.customerIOClient.delete(userId)
    }
}
