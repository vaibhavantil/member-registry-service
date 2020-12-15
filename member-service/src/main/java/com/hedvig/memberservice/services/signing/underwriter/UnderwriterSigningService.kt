package com.hedvig.memberservice.services.signing.underwriter

import com.hedvig.memberservice.services.signing.underwriter.strategy.UnderwriterSessionCompletedData
import com.hedvig.memberservice.web.dto.UnderwriterStartSignSessionRequest
import com.hedvig.memberservice.web.dto.UnderwriterStartSignSessionResponse
import java.util.UUID

interface UnderwriterSigningService {

    fun startSign(memberId: Long, request: UnderwriterStartSignSessionRequest): UnderwriterStartSignSessionResponse

    fun isUnderwriterHandlingSignSession(orderReference: UUID): Boolean

    fun signSessionWasCompleted(signSessionReference: UUID, data: UnderwriterSessionCompletedData)
}

