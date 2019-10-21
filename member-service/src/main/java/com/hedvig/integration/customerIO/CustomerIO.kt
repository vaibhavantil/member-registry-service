package com.hedvig.integration.customerIO

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cloud.openfeign.EnableFeignClients
import org.springframework.stereotype.Component

@Component
@EnableFeignClients
class CustomerIO @Autowired constructor(
    private val customerIOClient: CustomerIOClient
){
    fun deleteCustomerIOUser(userId: String) {
        this.customerIOClient.delete(userId)
    }
}
