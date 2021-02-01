package com.hedvig.memberservice.services.trustpilot.api

data class TrustpilotReviewLinkRequestDto(
    val referenceId: String,
    val email: String,
    val name: String,
    val locale: String
)
