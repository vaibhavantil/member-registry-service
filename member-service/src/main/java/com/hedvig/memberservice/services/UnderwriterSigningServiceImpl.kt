package com.hedvig.memberservice.services

import com.hedvig.external.authentication.dto.StartNorwegianAuthenticationResult
import com.hedvig.integration.underwriter.UnderwriterClient
import com.hedvig.integration.underwriter.dtos.SignRequest
import com.hedvig.memberservice.entities.UnderwriterSignSessionEntity
import com.hedvig.memberservice.query.SignedMemberRepository
import com.hedvig.memberservice.query.UnderwriterSignSessionRepository
import com.hedvig.memberservice.query.saveOrUpdateReusableSession
import com.hedvig.memberservice.services.dto.StartNorwegianBankIdSignResponse
import com.hedvig.memberservice.services.dto.StartSwedishBankIdSignResponse
import org.springframework.stereotype.Service
import java.util.*

@Service
class UnderwriterSigningServiceImpl(
    private val underwriterSignSessionRepository: UnderwriterSignSessionRepository,
    private val underwriterClient: UnderwriterClient,
    private val swedishBankIdSigningService: SwedishBankIdSigningService,
    private val norwegianSigningService: NorwegianSigningService,
    private val signedMemberRepository: SignedMemberRepository
) : UnderwriterSigningService {

    override fun startSwedishBankIdSignSession(underwriterSessionRef: UUID, memberId: Long, ssn: String, ipAddress: String, isSwitching: Boolean): StartSwedishBankIdSignResponse {
        if (isAlreadySigned(ssn)) {
            return StartSwedishBankIdSignResponse(
                autoStartToken = null,
                internalErrorMessage = "Member already signed"
            )
        }

        val response = swedishBankIdSigningService.startSign(memberId, ssn, ipAddress, isSwitching)

        underwriterSignSessionRepository.saveOrUpdateReusableSession(underwriterSessionRef, UUID.fromString(response.bankIdOrderResponse.orderRef))

        return StartSwedishBankIdSignResponse(response.bankIdOrderResponse.autoStartToken)
    }

    override fun startNorwegianBankIdSignSession(underwriterSessionRef: UUID, memberId: Long, ssn: String): StartNorwegianBankIdSignResponse {
        if (isAlreadySigned(ssn)) {
            return StartNorwegianBankIdSignResponse(
                redirectUrl = null,
                internalErrorMessage = "Member already signed"
            )
        }

        return when (val response = norwegianSigningService.startSign(memberId, ssn)) {
            is StartNorwegianAuthenticationResult.Success -> {
                underwriterSignSessionRepository.saveOrUpdateReusableSession(underwriterSessionRef, response.orderReference)

                StartNorwegianBankIdSignResponse(response.redirectUrl)
            }
            is StartNorwegianAuthenticationResult.Failed -> StartNorwegianBankIdSignResponse(
                redirectUrl = null,
                errorMessages = response.errors
            )
        }
    }

    override fun isUnderwriterHandlingSignSession(orderReference: UUID): Boolean =
        underwriterSignSessionRepository.findBySignReference(orderReference) != null

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

    override fun norwegianBankIdSignSessionWasCompleted(orderReference: UUID) {
        val session = underwriterSignSessionRepository.findBySignReference(orderReference)
            ?: throw IllegalCallerException("Called norwegianBankIdSignSessionWasCompleted but could not find underwriter sign session use isUnderwriterIsHandlingSignSession before calling this method")

        underwriterClient.singSessionComplete(session.underwriterSignSessionReference)
    }

    private fun isAlreadySigned(ssn: String): Boolean =
        signedMemberRepository.findBySsn(ssn).isPresent
}