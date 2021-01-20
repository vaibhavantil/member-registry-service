package com.hedvig.memberservice.external.trustpilot

data class TrustpilotReviewLinkRequestDto(
    val referenceId: String,
    val email: String,
    val name: String,
    val locale: String
)
