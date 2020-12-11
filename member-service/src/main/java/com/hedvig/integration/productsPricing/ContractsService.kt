package com.hedvig.integration.productsPricing

import org.springframework.stereotype.Service

@Service
class ContractsService(private val productClient: ProductPricingClient) {
    fun hasContract(memberId: Long) = productClient.hasContract(memberId).body!!
}
