package com.hedvig.memberservice.services.signing.underwriter.strategy

import com.hedvig.memberservice.web.dto.UnderwriterStartSignSessionRequest
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class StartSignSessionStrategyService(
    private val startSwedishBankIdSignSessionStrategy: StartSwedishBankIdSignSessionStrategy,
    private val startRedirectBankIdSignSessionStrategy: StartRedirectBankIdSignSessionStrategy,
    private val startSimpleSignSessionStrategy: StartSimpleSignSessionStrategy
) {
    fun startSignSession(memberId: Long, request: UnderwriterStartSignSessionRequest) = when (request) {
        is UnderwriterStartSignSessionRequest.SwedishBankId -> startSwedishBankIdSignSessionStrategy.startSignSession(memberId, request)
        is UnderwriterStartSignSessionRequest.BankIdRedirect -> startRedirectBankIdSignSessionStrategy.startSignSession(memberId, request)
        is UnderwriterStartSignSessionRequest.SimpleSign -> startSimpleSignSessionStrategy.startSignSession(memberId, request)
    }

    fun signSessionWasCompleted(underwriterSignSessionReference: UUID, data: UnderwriterSessionCompletedData) = when (data) {
        is UnderwriterSessionCompletedData.SwedishBankId -> startSwedishBankIdSignSessionStrategy.signSessionWasCompleted(underwriterSignSessionReference, data)
        is UnderwriterSessionCompletedData.BankIdRedirect -> startRedirectBankIdSignSessionStrategy.signSessionWasCompleted(underwriterSignSessionReference, data)
        is UnderwriterSessionCompletedData.SimpleSign -> startSimpleSignSessionStrategy.signSessionWasCompleted(underwriterSignSessionReference, data)
    }
}
