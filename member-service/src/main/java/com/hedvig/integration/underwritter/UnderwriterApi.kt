package com.hedvig.integration.underwritter

import com.hedvig.integration.underwritter.dtos.SignRequest
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
}
