package com.hedvig.integration.underwritter

import com.hedvig.integration.underwritter.dtos.QuoteToSignStatusDTO
import com.hedvig.integration.underwritter.dtos.SignRequest
import feign.FeignException
import org.springframework.stereotype.Service

@Service
class UnderwriterApi(private val underwriterClient: UnderwriterClient) {

    fun memberSigned(
        memberId: String,
        referenceToken: String,
        signature: String,
        oscpResponse: String
    ) {
        underwriterClient.memberSigned(memberId, SignRequest(referenceToken, signature, oscpResponse))
    }

    fun hasQuoteToSign(memberId: String): QuoteToSignStatusDTO {
        try {
            val response = underwriterClient.getQuoteFromMemberId(memberId).body!!

            return QuoteToSignStatusDTO(
                isEligibleToSign = true,
                isSwitching = response.currentInsurer != null
            )
        } catch (feignException: FeignException) {
            if (feignException.status() == 404) {
                return QuoteToSignStatusDTO(
                    isEligibleToSign = false,
                    isSwitching = false
                )
            }

            throw feignException
        }
    }
}
