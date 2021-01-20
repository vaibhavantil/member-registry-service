package com.hedvig.memberservice.services.trustpilot

import java.util.Locale;

interface TrustpilotReviewService {
    fun generateTrustpilotReviewInvitation(
        memberId: Long,
        email: String,
        name: String,
        locale: Locale?
    ): TrustpilotReviewInvitation?
}
