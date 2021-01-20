package com.hedvig.memberservice.services.trustpilot

import com.hedvig.memberservice.external.trustpilot.TrustpilotClient
import com.hedvig.memberservice.external.trustpilot.TrustpilotReviewLinkRequestDto
import com.hedvig.memberservice.util.logger
import com.hedvig.resolver.LocaleResolver
import org.springframework.stereotype.Service
import java.util.Locale

@Service
class TrustpilotReviewServiceImpl(
    private val trustpilotClient: TrustpilotClient
): TrustpilotReviewService {

    companion object {
        // fetched through their public API by looking up www.hedvig.com (name of our service in their system)
        private const val hedvigBusinessUnitId = "5b62ebf41788620001d3c4ae"
    }

    override fun generateTrustpilotReviewInvitation(
        memberId: Long,
        email: String,
        name: String,
        locale: Locale?
    ): TrustpilotReviewInvitation? {
        val locale = locale ?: LocaleResolver.DEFAULT_LOCALE
        return try {
            val body = TrustpilotReviewLinkRequestDto(
                referenceId = memberId.toString(),
                email = email,
                name = name,
                locale = "${locale.language}-${locale.country}"
            )
            val response = trustpilotClient.createReviewLink(hedvigBusinessUnitId, body)

            logger.info("Trustpilot review link created for member $memberId, link id = ${response.id}")
            TrustpilotReviewInvitation(response.id, response.url)
        } catch (exception: Exception) {
            logger.warn("Trustpilot API call failed", exception)
            null
        }
    }
}
