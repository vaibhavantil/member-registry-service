package com.hedvig.memberservice.services.trustpilot

import com.hedvig.memberservice.external.trustpilot.TrustpilotClient
import com.hedvig.memberservice.external.trustpilot.TrustpilotReviewLinkRequestDto
import com.hedvig.memberservice.external.trustpilot.TrustpilotReviewLinkResponseDto
import com.hedvig.memberservice.util.logger
import com.hedvig.resolver.LocaleResolver
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.util.*

@Service
class TrustpilotReviewServiceImpl(
    private val trustpilotClient: TrustpilotClient,
    @Value("\${trustpilot.businessUnitId}") val businessUnitId: String
): TrustpilotReviewService {
    override fun generateTrustpilotReviewInvitation(
        memberId: Long,
        email: String,
        name: String,
        locale: Locale?
    ): TrustpilotReviewInvitation? {
        val locale = locale ?: LocaleResolver.DEFAULT_LOCALE
        return try {
            val response = trustpilotClient.createReviewLink(
                businessUnitId,
                TrustpilotReviewLinkRequestDto(
                    memberId.toString(), email, name, "${locale.language}-${locale.country}"
                )
            )
            logger.info("Trustpilot review link created for member $memberId, link id = ${response.id}")
            TrustpilotReviewInvitation(response.id, response.url)
        } catch (exception: Exception) {
            logger.warn("Trustpilot API call failed", exception)
            null
        }
    }
}
