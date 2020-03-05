package com.hedvig.memberservice.services

import com.hedvig.external.authentication.dto.StartNorwegianAuthenticationResult
import com.hedvig.memberservice.entities.UnderwriterSignSessionEntity
import com.hedvig.memberservice.query.UnderwriterSignSessionRepository
import com.hedvig.memberservice.services.dto.StartNorwegianBankIdSignResponse
import com.hedvig.memberservice.services.dto.StartSwedishBankIdSignResponse
import org.springframework.stereotype.Service
import java.util.*

@Service
class UnderwriterSigningServiceImpl(
    private val underwriterSignSessionRepository: UnderwriterSignSessionRepository,
    private val swedishBankIdSigningService: SwedishBankIdSigningService,
    private val norwegianSigningService: NorwegianSigningService
): UnderwriterSigningService {

    override fun startSwedishBankIdSignSession(underwriterSessionRef: UUID, memberId: Long, ssn: String, ipAddress: String, isSwitching: Boolean): StartSwedishBankIdSignResponse {
        val response = swedishBankIdSigningService.startSign(memberId, ssn, ipAddress, isSwitching)

        underwriterSignSessionRepository.save(UnderwriterSignSessionEntity(underwriterSessionRef, UUID.fromString(response.bankIdOrderResponse.orderRef)))

        return StartSwedishBankIdSignResponse(response.bankIdOrderResponse.autoStartToken)
    }

    override fun startNorwegianBankIdSignSession(underwriterSessionRef: UUID, memberId: Long, ssn: String?): StartNorwegianBankIdSignResponse {
        return when (val response = norwegianSigningService.startSign(memberId, ssn)) {
            is StartNorwegianAuthenticationResult.Success -> {
                underwriterSignSessionRepository.save(UnderwriterSignSessionEntity(underwriterSessionRef, response.orderReference))

                StartNorwegianBankIdSignResponse(response.redirectUrl)
            }
            is StartNorwegianAuthenticationResult.Failed -> StartNorwegianBankIdSignResponse(null, response.errors)
        }
    }
}
