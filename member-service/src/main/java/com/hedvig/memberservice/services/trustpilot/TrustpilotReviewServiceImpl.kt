package com.hedvig.memberservice.services.trustpilot

import com.hedvig.memberservice.external.trustpilot.TrustpilotClient
import com.hedvig.memberservice.external.trustpilot.TrustpilotReviewLinkRequestDto
import com.hedvig.memberservice.external.trustpilot.TrustpilotReviewLinkResponseDto
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class TrustpilotReviewServiceImpl(
    private val trustpilotClient: TrustpilotClient,
    @Value("\${trustpilot.businessUnitId}") val businessUnitId: String
): TrustpilotReviewService {
    override fun generateTrustpilotReviewLinkForMember(request: TrustpilotReviewLinkRequestDto
        ): TrustpilotReviewLinkResponseDto {
        return TrustpilotReviewLinkResponseDto(id = "123", url = "www.test.com")
//        return trustpilotClient.createReviewLink(businessUnitId, request)
    }
}
