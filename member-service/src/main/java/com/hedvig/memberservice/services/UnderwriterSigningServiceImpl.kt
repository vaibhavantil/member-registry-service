package com.hedvig.memberservice.services

import com.hedvig.external.authentication.dto.StartNorwegianAuthenticationResult
import com.hedvig.integration.underwriter.UnderwriterClient
import com.hedvig.integration.underwriter.dtos.SignRequest
import com.hedvig.memberservice.query.SignedMemberRepository
import com.hedvig.memberservice.query.UnderwriterSignSessionRepository
import com.hedvig.memberservice.query.saveOrUpdateReusableSession
import com.hedvig.memberservice.services.dto.StartNorwegianBankIdSignResponse
import com.hedvig.memberservice.services.dto.StartSwedishBankIdSignResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component
import org.springframework.stereotype.Service
import java.net.URL
import java.util.*



@Service
class UnderwriterSigningServiceImpl(
    private val underwriterSignSessionRepository: UnderwriterSignSessionRepository,
    private val underwriterClient: UnderwriterClient,
    private val swedishBankIdSigningService: SwedishBankIdSigningService,
    private val norwegianSigningService: NorwegianSigningService,
    private val signedMemberRepository: SignedMemberRepository,
    @Value("\${zignsec.validSigningTargetHosts}")
    private val validTargetHosts: Array<String>
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

    override fun startNorwegianBankIdSignSession(underwriterSessionRef: UUID, memberId: Long, ssn: String, targetUrl: String, failedTargetUrl: String): StartNorwegianBankIdSignResponse {
        if (!hasValidHost(targetUrl) || !hasValidHost(failedTargetUrl)) {
            return StartNorwegianBankIdSignResponse(
                redirectUrl = null,
                internalErrorMessage = "Not an valid target url"
            )
        }

        if (isAlreadySigned(ssn)) {
            return StartNorwegianBankIdSignResponse(
                redirectUrl = null,
                internalErrorMessage = "Member already signed"
            )
        }

        return when (val response = norwegianSigningService.startSign(memberId, ssn, targetUrl, failedTargetUrl)) {
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

    private fun hasValidHost(url: String): Boolean =
        validTargetHosts.contains(URL(url).host)

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
