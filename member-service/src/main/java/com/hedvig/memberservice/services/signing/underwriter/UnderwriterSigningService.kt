package com.hedvig.memberservice.services.signing.underwriter

import com.hedvig.memberservice.web.dto.UnderwriterStartSignSessionRequest
import com.hedvig.memberservice.web.dto.UnderwriterStartSignSessionResponse
import java.util.UUID

interface UnderwriterSigningService {

    fun startSign(memberId: Long, request: UnderwriterStartSignSessionRequest): UnderwriterStartSignSessionResponse

    fun isUnderwriterHandlingSignSession(orderReference: UUID): Boolean

    fun swedishBankIdSignSessionWasCompleted(orderReference: String, signature: String, oscpResponse: String)

    fun underwriterSignSessionWasCompleted(orderReference: UUID)
}

