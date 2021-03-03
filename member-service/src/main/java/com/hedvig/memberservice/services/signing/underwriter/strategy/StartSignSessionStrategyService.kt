package com.hedvig.memberservice.services.signing.underwriter.strategy

import com.hedvig.memberservice.web.dto.UnderwriterStartSignSessionRequest
import com.hedvig.memberservice.web.dto.UnderwriterStartSignSessionResponse
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class StartSignSessionStrategyService(
    private val startSwedishBankIdSignSessionStrategy: StartSwedishBankIdSignSessionStrategy,
    private val startRedirectBankIdSignSessionStrategy: StartRedirectBankIdSignSessionStrategy,
    private val startSimpleSignSessionStrategy: StartSimpleSignSessionStrategy
) {
    fun startSignSession(memberId: Long, request: UnderwriterStartSignSessionRequest, storeUnderwriterSignSession: (UUID, SignStrategy) -> Unit): UnderwriterStartSignSessionResponse = when (request) {
        is UnderwriterStartSignSessionRequest.SwedishBankId -> startSwedishBankIdSignSessionStrategy.startSignSession(memberId, request, storeUnderwriterSignSession)
        is UnderwriterStartSignSessionRequest.BankIdRedirect -> startRedirectBankIdSignSessionStrategy.startSignSession(memberId, request, storeUnderwriterSignSession)
        is UnderwriterStartSignSessionRequest.SimpleSign -> startSimpleSignSessionStrategy.startSignSession(memberId, request, storeUnderwriterSignSession)
    }

    fun signSessionWasCompleted(underwriterSignSessionReference: UUID, data: UnderwriterSessionCompletedData) = when (data) {
        is UnderwriterSessionCompletedData.SwedishBankId -> startSwedishBankIdSignSessionStrategy.signSessionWasCompleted(underwriterSignSessionReference, data)
        is UnderwriterSessionCompletedData.BankIdRedirect -> startRedirectBankIdSignSessionStrategy.signSessionWasCompleted(underwriterSignSessionReference, data)
        is UnderwriterSessionCompletedData.SimpleSign -> startSimpleSignSessionStrategy.signSessionWasCompleted(underwriterSignSessionReference, data)
    }
}
