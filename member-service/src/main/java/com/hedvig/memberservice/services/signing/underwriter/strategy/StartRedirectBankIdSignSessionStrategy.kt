package com.hedvig.memberservice.services.signing.underwriter.strategy

import com.hedvig.external.authentication.dto.StartZignSecAuthenticationResult
import com.hedvig.memberservice.services.signing.zignsec.ZignSecSigningService
import com.hedvig.memberservice.web.dto.UnderwriterStartSignSessionRequest
import com.hedvig.memberservice.web.dto.UnderwriterStartSignSessionResponse
import com.hedvig.memberservice.web.dto.toZignSecAuthenticationMarket
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.lang.RuntimeException
import java.net.URL
import java.util.UUID

@Service
class StartRedirectBankIdSignSessionStrategy(
    private val zignSecSigningService: ZignSecSigningService,
    @Value("\${zignsec.validSigningTargetHosts}")
    private val validTargetHosts: Array<String>,
    private val commonSessionCompletion: CommonSessionCompletion
) : StartSignSessionStrategy<UnderwriterStartSignSessionRequest.BankIdRedirect, UnderwriterStartSignSessionResponse.BankIdRedirect, UnderwriterSessionCompletedData.BankIdRedirect> {

    override val signStrategy = SignStrategy.REDIRECT_BANK_ID

    override fun startSignSession(memberId: Long, request: UnderwriterStartSignSessionRequest.BankIdRedirect): Triple<UUID?, UnderwriterStartSignSessionResponse.BankIdRedirect, SignStrategy> {
        if (!hasValidHost(request.successUrl) || !hasValidHost(request.failUrl)) {
            return Triple(
                null,
                UnderwriterStartSignSessionResponse.BankIdRedirect(
                    redirectUrl = null,
                    internalErrorMessage = "Not a valid target url"
                ),
                signStrategy
            )
        }

        val response = zignSecSigningService.startSign(
            memberId,
            request.nationalIdentification.identification,
            request.successUrl,
            request.failUrl,
            request.country.toZignSecAuthenticationMarket()
        )

        return when (response) {
            is StartZignSecAuthenticationResult.Success -> {
                Triple(
                    response.orderReference,
                    UnderwriterStartSignSessionResponse.BankIdRedirect(response.redirectUrl.trim()),
                    signStrategy
                )
            }
            is StartZignSecAuthenticationResult.Failed ->
                Triple(
                    null,
                    UnderwriterStartSignSessionResponse.BankIdRedirect(
                        redirectUrl = null,
                        errorMessages = response.errors
                    ),
                    signStrategy
                )
            is StartZignSecAuthenticationResult.StaticRedirect ->
                throw RuntimeException("We should not do StaticRedirect on signing")
        }
    }

    private fun hasValidHost(url: String): Boolean =
        validTargetHosts.contains(URL(url).host)

    override fun signSessionWasCompleted(signSessionReference: UUID, data: UnderwriterSessionCompletedData.BankIdRedirect) {
        commonSessionCompletion.signSessionWasCompleted(signSessionReference)
    }
}
