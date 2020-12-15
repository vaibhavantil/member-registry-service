package com.hedvig.memberservice.services.signing.underwriter

import com.hedvig.integration.underwriter.UnderwriterClient
import com.hedvig.integration.underwriter.dtos.SignRequest
import com.hedvig.memberservice.query.SignedMemberRepository
import com.hedvig.memberservice.query.UnderwriterSignSessionRepository
import com.hedvig.memberservice.query.saveOrUpdateReusableSession
import com.hedvig.memberservice.services.signing.underwriter.strategy.StartSignSessionStrategyService
import com.hedvig.memberservice.web.dto.UnderwriterStartSignSessionRequest
import com.hedvig.memberservice.web.dto.UnderwriterStartSignSessionResponse
import org.springframework.stereotype.Service
import java.util.*

@Service
class UnderwriterSigningServiceImpl(
    private val underwriterSignSessionRepository: UnderwriterSignSessionRepository,
    private val underwriterClient: UnderwriterClient,
    private val signedMemberRepository: SignedMemberRepository,
    private val startSignSessionStrategyService: StartSignSessionStrategyService
) : UnderwriterSigningService {

    override fun startSign(memberId: Long, request: UnderwriterStartSignSessionRequest): UnderwriterStartSignSessionResponse {
        if (signedMemberRepository.findBySsn(request.nationalIdentification.identification).isPresent) {
            return request.createErrorResponse("Could not start sign")
        }

        val (signSessionReference, response) = startSignSessionStrategyService.startSignSession(memberId, request)

        signSessionReference?.let {
            underwriterSignSessionRepository.saveOrUpdateReusableSession(request.underwriterSessionReference, it)
        }

        return response
    }

    override fun isUnderwriterHandlingSignSession(signSessionReference: UUID): Boolean =
        underwriterSignSessionRepository.findBySignReference(signSessionReference) != null

    override fun swedishBankIdSignSessionWasCompleted(orderReference: String, signature: String, oscpResponse: String) {
        val session = underwriterSignSessionRepository.findBySignReference(UUID.fromString(orderReference))
            ?: throw IllegalCallerException("Called swedishBankIdSignSessionWasCompleted but could not find underwriter sign session use isUnderwriterIsHandlingSignSession before calling this method")

        underwriterClient.swedishBankIdSingComplete(
            session.underwriterSignSessionReference,
            SignRequest(
                orderReference,
                signature,
                oscpResponse
            )
        )
    }

    override fun underwriterSignSessionWasCompleted(orderReference: UUID) {
        val session = underwriterSignSessionRepository.findBySignReference(orderReference)
            ?: throw IllegalCallerException("Called underwriterSignSessionWasCompleted but could not find underwriter sign session use isUnderwriterIsHandlingSignSession before calling this method")

        underwriterClient.singSessionComplete(session.underwriterSignSessionReference)
    }
}
