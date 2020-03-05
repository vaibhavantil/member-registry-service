package com.hedvig.memberservice.services

import com.hedvig.external.authentication.dto.NorwegianAuthenticationResponseError
import com.hedvig.external.authentication.dto.StartNorwegianAuthenticationResult
import com.hedvig.memberservice.entities.UnderwriterSignSessionEntity
import com.hedvig.memberservice.query.SignedMemberRepository
import com.hedvig.memberservice.query.UnderwriterSignSessionRepository
import com.hedvig.memberservice.services.dto.StartNorwegianBankIdSignResponse
import com.hedvig.memberservice.services.dto.StartSwedishBankIdSignResponse
import org.springframework.stereotype.Service
import java.util.*

@Service
class UnderwriterSigningServiceImpl(
    private val underwriterSignSessionRepository: UnderwriterSignSessionRepository,
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

        underwriterSignSessionRepository.save(UnderwriterSignSessionEntity(underwriterSessionRef, UUID.fromString(response.bankIdOrderResponse.orderRef)))

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
                underwriterSignSessionRepository.save(UnderwriterSignSessionEntity(underwriterSessionRef, response.orderReference))

                StartNorwegianBankIdSignResponse(response.redirectUrl)
            }
            is StartNorwegianAuthenticationResult.Failed -> StartNorwegianBankIdSignResponse(
                redirectUrl = null,
                errorMessages = response.errors
            )
        }
    }

    private fun isAlreadySigned(ssn: String): Boolean =
        signedMemberRepository.findBySsn(ssn).isPresent
}
