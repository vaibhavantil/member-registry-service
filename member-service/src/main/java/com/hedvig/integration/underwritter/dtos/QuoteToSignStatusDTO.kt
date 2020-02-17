package com.hedvig.integration.underwritter.dtos

data class QuoteToSignStatusDTO(
    val isEligibleToSign: Boolean,
    val isSwitching: Boolean
)
