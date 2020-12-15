package com.hedvig.memberservice.services.signing.underwriter.strategy

import com.hedvig.memberservice.web.dto.UnderwriterStartSignSessionRequest
import org.springframework.stereotype.Service

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
}
