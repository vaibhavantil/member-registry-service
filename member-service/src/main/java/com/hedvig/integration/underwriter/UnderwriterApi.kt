package com.hedvig.integration.underwriter

import com.hedvig.integration.underwriter.dtos.QuoteDto
import com.hedvig.integration.underwriter.dtos.QuoteState
import com.hedvig.integration.underwriter.dtos.QuoteToSignStatusDto
import com.hedvig.integration.underwriter.dtos.SignMethod
import com.hedvig.integration.underwriter.dtos.SignRequest
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

    fun hasQuoteToSign(memberId: String): QuoteToSignStatusDto {
        try {
            val response = underwriterClient.getQuoteFromMemberId(memberId).body!!

            return if (response.state == QuoteState.QUOTED){
                QuoteToSignStatusDto.EligibleToSign(
                    response.currentInsurer != null,
                    response.getSignMethod()
                )
            } else {
                QuoteToSignStatusDto.NotEligibleToSign
            }
        } catch (feignException: FeignException) {
            if (feignException.status() == 404) {
                return QuoteToSignStatusDto.NotEligibleToSign
            }

            throw feignException
        }
    }

    fun QuoteDto.getSignMethod() = when {
        this.data.type == "apartment" || this.data.type == "house"-> {
            SignMethod.SWEDISH_BANK_ID
        }
        this.data.type == "norwegianHomeContentsData" || this.data.type == "norwegianTravelData"-> {
            SignMethod.NORWEGIAN_BANK_ID
        }
        else -> {
            throw RuntimeException("Could not map [QuoteDto: $this] to sign method")
        }
    }
}
