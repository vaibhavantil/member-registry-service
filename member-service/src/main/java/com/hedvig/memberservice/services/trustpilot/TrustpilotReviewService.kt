package com.hedvig.memberservice.services.trustpilot

import com.hedvig.memberservice.external.trustpilot.TrustpilotReviewLinkRequestDto
import com.hedvig.memberservice.external.trustpilot.TrustpilotReviewLinkResponseDto

interface TrustpilotReviewService {
    fun generateTrustpilotReviewLinkForMember(request: TrustpilotReviewLinkRequestDto): TrustpilotReviewLinkResponseDto?
}
