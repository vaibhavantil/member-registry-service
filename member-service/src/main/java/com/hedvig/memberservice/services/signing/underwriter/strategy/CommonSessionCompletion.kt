package com.hedvig.memberservice.services.signing.underwriter.strategy

import com.hedvig.integration.underwriter.UnderwriterClient
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class CommonSessionCompletion(
    private val underwriterClient: UnderwriterClient
) {
    fun signSessionWasCompleted(underwriterSignSessionReference: UUID) {
        underwriterClient.signSessionComplete(underwriterSignSessionReference)
    }
}
