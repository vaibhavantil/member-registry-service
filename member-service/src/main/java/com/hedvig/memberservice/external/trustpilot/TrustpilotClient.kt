package com.hedvig.memberservice.external.trustpilot

import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.*

@FeignClient(
    name = "trustpilotClient",
    url = "https://invitations-api.trustpilot.com/v1/private",
    configuration = [TrustpilotFeignConfig::class]
)
interface TrustpilotClient {

    @PostMapping("/business-units/{businessUnitId}/invitation-links")
    fun createReviewLink(
        @PathVariable("businessUnitId") businessUnitId: String,
        @RequestBody body: TrustpilotReviewLinkRequestDto
    ): TrustpilotReviewLinkResponseDto
}

data class TrustpilotReviewLinkRequestDto(
    val referenceId: String,
    val email: String,
    val name: String,
    val locale: String
)

data class TrustpilotReviewLinkResponseDto(
    val id: String,
    val url: String
)
