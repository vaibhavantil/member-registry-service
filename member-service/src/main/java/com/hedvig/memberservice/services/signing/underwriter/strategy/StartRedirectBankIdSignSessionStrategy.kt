package com.hedvig.memberservice.services.signing.underwriter.strategy

import com.hedvig.external.authentication.dto.StartZignSecAuthenticationResult
import com.hedvig.memberservice.services.signing.zignsec.ZignSecSigningService
import com.hedvig.memberservice.web.dto.UnderwriterStartSignSessionRequest
import com.hedvig.memberservice.web.dto.UnderwriterStartSignSessionResponse
import com.hedvig.memberservice.web.dto.toZignSecAuthenticationMarket
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.net.URL
import java.util.UUID

@Service
class StartRedirectBankIdSignSessionStrategy(
    private val zignSecSigningService: ZignSecSigningService,
    @Value("\${zignsec.validSigningTargetHosts}")
    private val validTargetHosts: Array<String>
) : StartSignSessionStrategy<UnderwriterStartSignSessionRequest.BankIdRedirect, UnderwriterStartSignSessionResponse.BankIdRedirect> {
    override fun startSignSession(memberId: Long, request: UnderwriterStartSignSessionRequest.BankIdRedirect): Pair<UUID?, UnderwriterStartSignSessionResponse.BankIdRedirect> {
        if (!hasValidHost(request.successUrl) || !hasValidHost(request.failUrl)) {
            return Pair(
                null,
                UnderwriterStartSignSessionResponse.BankIdRedirect(
                    redirectUrl = null,
                    internalErrorMessage = "Not a valid target url"
                )
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
                Pair(response.orderReference, UnderwriterStartSignSessionResponse.BankIdRedirect(response.redirectUrl.trim()))
            }
            is StartZignSecAuthenticationResult.Failed ->
                Pair(
                    null,
                    UnderwriterStartSignSessionResponse.BankIdRedirect(
                        redirectUrl = null,
                        errorMessages = response.errors
                    )
                )
        }
    }

    private fun hasValidHost(url: String): Boolean =
        validTargetHosts.contains(URL(url).host)
}
