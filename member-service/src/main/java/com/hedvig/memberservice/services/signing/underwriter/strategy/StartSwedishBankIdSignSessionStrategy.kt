package com.hedvig.memberservice.services.signing.underwriter.strategy

import com.hedvig.integration.underwriter.UnderwriterClient
import com.hedvig.integration.underwriter.dtos.SignRequest
import com.hedvig.memberservice.services.signing.sweden.SwedishBankIdSigningService
import com.hedvig.memberservice.web.dto.UnderwriterStartSignSessionRequest
import com.hedvig.memberservice.web.dto.UnderwriterStartSignSessionResponse
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class StartSwedishBankIdSignSessionStrategy(
    private val swedishBankIdSigningService: SwedishBankIdSigningService,
    private val underwriterClient: UnderwriterClient
) : StartSignSessionStrategy<UnderwriterStartSignSessionRequest.SwedishBankId, UnderwriterStartSignSessionResponse.SwedishBankId, UnderwriterSessionCompletedData.SwedishBankId> {

    override fun startSignSession(memberId: Long, request: UnderwriterStartSignSessionRequest.SwedishBankId): Pair<UUID?, UnderwriterStartSignSessionResponse.SwedishBankId> {
        val response = swedishBankIdSigningService.startSign(
            memberId,
            request.nationalIdentification.identification,
            request.ipAddress,
            request.isSwitching
        )

        return Pair(
            UUID.fromString(response.bankIdOrderResponse.orderRef),
            UnderwriterStartSignSessionResponse.SwedishBankId(
                response.bankIdOrderResponse.autoStartToken
            )
        )
    }

    override fun signSessionWasCompleted(underwriterSignSessionReference: UUID, data: UnderwriterSessionCompletedData.SwedishBankId) {
        underwriterClient.swedishBankIdSingComplete(
            underwriterSignSessionReference,
            SignRequest(
                data.referenceToken,
                data.signature,
                data.oscpResponse
            )
        )
    }
}
