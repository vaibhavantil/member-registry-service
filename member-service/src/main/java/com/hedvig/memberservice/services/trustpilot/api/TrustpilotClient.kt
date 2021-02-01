package com.hedvig.memberservice.services.trustpilot.api

import org.springframework.web.bind.annotation.*

interface TrustpilotClient {

    @PostMapping("/business-units/{businessUnitId}/invitation-links")
    fun createReviewLink(
        @PathVariable("businessUnitId") businessUnitId: String,
        @RequestBody body: TrustpilotReviewLinkRequestDto
    ): TrustpilotReviewLinkResponseDto
}

