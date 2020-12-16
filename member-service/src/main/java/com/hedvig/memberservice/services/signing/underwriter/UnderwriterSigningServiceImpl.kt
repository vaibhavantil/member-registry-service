package com.hedvig.memberservice.services.signing.underwriter

import com.hedvig.memberservice.query.SignedMemberRepository
import com.hedvig.memberservice.query.UnderwriterSignSessionRepository
import com.hedvig.memberservice.query.saveOrUpdateReusableSession
import com.hedvig.memberservice.services.signing.underwriter.strategy.StartSignSessionStrategyService
import com.hedvig.memberservice.services.signing.underwriter.strategy.UnderwriterSessionCompletedData
import com.hedvig.memberservice.web.dto.UnderwriterStartSignSessionRequest
import com.hedvig.memberservice.web.dto.UnderwriterStartSignSessionResponse
import org.springframework.stereotype.Service
import java.util.*

@Service
class UnderwriterSigningServiceImpl(
    private val underwriterSignSessionRepository: UnderwriterSignSessionRepository,
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

    override fun signSessionWasCompleted(signSessionReference: UUID, data: UnderwriterSessionCompletedData) {
        val session = underwriterSignSessionRepository.findBySignReference(signSessionReference)
            ?: throw IllegalCallerException("Called underwriterSignSessionWasCompleted but could not find underwriter sign session use isUnderwriterIsHandlingSignSession before calling this method")

        startSignSessionStrategyService.signSessionWasCompleted(session.underwriterSignSessionReference, data)
    }
}
