package com.hedvig.memberservice.services.signing.underwriter

import com.hedvig.external.authentication.dto.StartZignSecAuthenticationResult
import com.hedvig.integration.underwriter.UnderwriterClient
import com.hedvig.integration.underwriter.dtos.SignRequest
import com.hedvig.memberservice.commands.models.ZignSecAuthenticationMarket
import com.hedvig.memberservice.query.SignedMemberRepository
import com.hedvig.memberservice.query.UnderwriterSignSessionRepository
import com.hedvig.memberservice.query.saveOrUpdateReusableSession
import com.hedvig.memberservice.services.signing.sweden.SwedishBankIdSigningService
import com.hedvig.memberservice.services.signing.zignsec.ZignSecSigningService
import com.hedvig.memberservice.services.signing.zignsec.dto.StartZignSecBankIdSignResponse
import com.hedvig.memberservice.services.signing.sweden.dto.StartSwedishBankIdSignResponse
import com.hedvig.memberservice.services.signing.simple.SimpleSigningService
import com.hedvig.memberservice.services.signing.sweden.dto.toUnderwriterStartSignSessionResponse
import com.hedvig.memberservice.services.signing.zignsec.dto.toUnderwriterStartSignSessionResponse
import com.hedvig.memberservice.web.dto.NationalIdentification
import com.hedvig.memberservice.web.dto.UnderwriterStartSignSessionRequest
import com.hedvig.memberservice.web.dto.UnderwriterStartSignSessionResponse
import com.hedvig.memberservice.web.dto.toZignSecAuthenticationMarket
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.net.URL
import java.util.*

@Service
class UnderwriterSigningServiceImpl(
    private val underwriterSignSessionRepository: UnderwriterSignSessionRepository,
    private val underwriterClient: UnderwriterClient,
    private val swedishBankIdSigningService: SwedishBankIdSigningService,
    private val zignSecSigningService: ZignSecSigningService,
    private val simpleSigningService: SimpleSigningService,
    private val signedMemberRepository: SignedMemberRepository,
    @Value("\${zignsec.validSigningTargetHosts}")
    private val validTargetHosts: Array<String>
) : UnderwriterSigningService {

    override fun startSwedishBankIdSignSession(underwriterSessionRef: UUID, memberId: Long, ssn: String, ipAddress: String, isSwitching: Boolean): StartSwedishBankIdSignResponse {
        ensureSsnIsNotSigned(ssn) { errorMessage ->
            return StartSwedishBankIdSignResponse(
                autoStartToken = null,
                internalErrorMessage = errorMessage
            )
        }

        val response = swedishBankIdSigningService.startSign(memberId, ssn, ipAddress, isSwitching)

        underwriterSignSessionRepository.saveOrUpdateReusableSession(underwriterSessionRef, UUID.fromString(response.bankIdOrderResponse.orderRef))

        return StartSwedishBankIdSignResponse(response.bankIdOrderResponse.autoStartToken)
    }

    override fun startNorwegianBankIdSignSession(
        underwriterSessionRef: UUID,
        memberId: Long,
        ssn: String,
        successUrl: String,
        failUrl: String
    ): StartZignSecBankIdSignResponse {
        return startZignSecSignSession(underwriterSessionRef, memberId, ssn, successUrl, failUrl, ZignSecAuthenticationMarket.NORWAY)
    }

    override fun startDanishBankIdSignSession(
        underwriterSessionRef: UUID,
        memberId: Long,
        ssn: String,
        successUrl: String,
        failUrl: String
    ): StartZignSecBankIdSignResponse {
        return startZignSecSignSession(underwriterSessionRef, memberId, ssn, successUrl, failUrl, ZignSecAuthenticationMarket.DENMARK)
    }

    private fun startZignSecSignSession(
        underwriterSessionRef: UUID,
        memberId: Long,
        ssn: String,
        successUrl: String,
        failUrl: String,
        zignSecAuthenticationMarket: ZignSecAuthenticationMarket
    ): StartZignSecBankIdSignResponse {
        if (!hasValidHost(successUrl) || !hasValidHost(failUrl)) {
            return StartZignSecBankIdSignResponse(
                redirectUrl = null,
                internalErrorMessage = "Not an valid target url"
            )
        }

        ensureSsnIsNotSigned(ssn) { errorMessage ->
            return StartZignSecBankIdSignResponse(
                redirectUrl = null,
                internalErrorMessage = errorMessage
            )
        }

        return when (val response = zignSecSigningService.startSign(memberId, ssn, successUrl, failUrl, zignSecAuthenticationMarket)) {
            is StartZignSecAuthenticationResult.Success -> {
                underwriterSignSessionRepository.saveOrUpdateReusableSession(underwriterSessionRef, response.orderReference)

                StartZignSecBankIdSignResponse(response.redirectUrl.trim())
            }
            is StartZignSecAuthenticationResult.Failed -> StartZignSecBankIdSignResponse(
                redirectUrl = null,
                errorMessages = response.errors
            )
        }
    }

    private fun hasValidHost(url: String): Boolean =
        validTargetHosts.contains(URL(url).host)

    override fun startSign(memberId: Long, request: UnderwriterStartSignSessionRequest): UnderwriterStartSignSessionResponse {
        return when (request) {
            is UnderwriterStartSignSessionRequest.SwedishBankId -> startSwedishBankIdSignSession(
                request.underwriterSessionReference, memberId, request.nationalIdentification.identification, request.ipAddress, request.isSwitching
            ).toUnderwriterStartSignSessionResponse()
            is UnderwriterStartSignSessionRequest.BankIdRedirect -> {
                startZignSecSignSession(
                    request.underwriterSessionReference,
                    memberId,
                    request.nationalIdentification.identification,
                    request.successUrl,
                    request.failUrl,
                    request.country.toZignSecAuthenticationMarket()
                ).toUnderwriterStartSignSessionResponse()
            }
            is UnderwriterStartSignSessionRequest.SimpleSign -> startSimpleSignSession(
                request.underwriterSessionReference, memberId, request.nationalIdentification
            )
        }
    }

    private fun startSimpleSignSession(underwriterSessionReference: UUID, memberId: Long, nationalIdentification: NationalIdentification): UnderwriterStartSignSessionResponse.SimpleSign {
        ensureSsnIsNotSigned(nationalIdentification.identification) { errorMessage ->
            return UnderwriterStartSignSessionResponse.SimpleSign(
                successfullyStarted = false,
                internalErrorMessage = errorMessage
            )
        }

        val signReference = simpleSigningService.startSign(memberId, nationalIdentification)
        underwriterSignSessionRepository.saveOrUpdateReusableSession(underwriterSessionReference, signReference)
        return UnderwriterStartSignSessionResponse.SimpleSign(successfullyStarted = true)
    }

    private inline fun <T> ensureSsnIsNotSigned(ssn: String, returner: (errorMessage: String) -> T): T? =
        if (signedMemberRepository.findBySsn(ssn).isPresent) {
            returner.invoke("Could not start sign")
        } else {
            null
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

    override fun underwriterSignSessionWasCompleted(orderReference: UUID) {
        val session = underwriterSignSessionRepository.findBySignReference(orderReference)
            ?: throw IllegalCallerException("Called underwriterSignSessionWasCompleted but could not find underwriter sign session use isUnderwriterIsHandlingSignSession before calling this method")

        underwriterClient.singSessionComplete(session.underwriterSignSessionReference)
    }

    private fun isAlreadySigned(ssn: String): Boolean =
        signedMemberRepository.findBySsn(ssn).isPresent
}
