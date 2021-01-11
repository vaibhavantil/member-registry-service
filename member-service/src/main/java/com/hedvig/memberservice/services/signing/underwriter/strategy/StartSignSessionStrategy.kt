package com.hedvig.memberservice.services.signing.underwriter.strategy

import com.hedvig.memberservice.web.dto.UnderwriterStartSignSessionRequest
import com.hedvig.memberservice.web.dto.UnderwriterStartSignSessionResponse
import java.util.UUID

interface StartSignSessionStrategy<in T : UnderwriterStartSignSessionRequest, out R : UnderwriterStartSignSessionResponse, in C: UnderwriterSessionCompletedData> {
    val signStrategy: SignStrategy
    fun startSignSession(memberId: Long, request: T): Triple<UUID?, R, SignStrategy>
    fun signSessionWasCompleted(signSessionReference: UUID, data: C)
}

