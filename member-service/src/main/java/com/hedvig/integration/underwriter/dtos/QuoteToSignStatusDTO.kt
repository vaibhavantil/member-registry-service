package com.hedvig.integration.underwriter.dtos

data class QuoteToSignStatusDTO(
    val isEligibleToSign: Boolean,
    val isSwitching: Boolean
)
