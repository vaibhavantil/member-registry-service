package com.hedvig.integration.productsPricing

import org.springframework.stereotype.Service

@Service
class ContractsService(private val productClient: ProductClient) {
    fun hasContract(memberId: Long): Boolean = productClient.hasContract(memberId).body!!
}
