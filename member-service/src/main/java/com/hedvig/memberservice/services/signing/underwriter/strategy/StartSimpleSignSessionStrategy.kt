package com.hedvig.memberservice.services.signing.underwriter.strategy

import com.hedvig.memberservice.services.signing.simple.SimpleSigningService
import com.hedvig.memberservice.web.dto.UnderwriterStartSignSessionRequest
import com.hedvig.memberservice.web.dto.UnderwriterStartSignSessionResponse
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class StartSimpleSignSessionStrategy(
    private val simpleSigningService: SimpleSigningService
) : StartSignSessionStrategy<UnderwriterStartSignSessionRequest.SimpleSign, UnderwriterStartSignSessionResponse.SimpleSign> {
    override fun startSignSession(memberId: Long, request: UnderwriterStartSignSessionRequest.SimpleSign): Pair<UUID?, UnderwriterStartSignSessionResponse.SimpleSign> {
        val signSession = simpleSigningService.startSign(memberId, request.nationalIdentification)

        return Pair(signSession, UnderwriterStartSignSessionResponse.SimpleSign(true))
    }
}
