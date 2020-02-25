package com.hedvig.integration.underwriter.dtos

sealed class QuoteToSignStatusDto {
    data class EligibleToSign(
        val isSwitching: Boolean,
        val signMethod: SignMethod
    ): QuoteToSignStatusDto()

    object NotEligibleToSign: QuoteToSignStatusDto()
}
