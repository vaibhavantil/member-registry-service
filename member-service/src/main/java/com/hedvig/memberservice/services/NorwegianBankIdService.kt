package com.hedvig.memberservice.services

import com.hedvig.external.authentication.NorwegianAuthentication
import com.hedvig.external.authentication.dto.NorwegianBankIdAuthenticationRequest
import org.springframework.stereotype.Service

@Service
class NorwegianBankIdService(
    private val norwegianAuthentication: NorwegianAuthentication
) {
    fun authenticate(request: NorwegianBankIdAuthenticationRequest) =
        norwegianAuthentication.auth(
            request
        )

    fun sign(memberId: String, ssn: String, acceptLanguage: String?) =
        norwegianAuthentication.sign(
            NorwegianBankIdAuthenticationRequest(
                memberId,
                ssn,
                acceptLanguage?.toTwoLetterLanguage() ?: "NO"
            )
        )

    fun getStatus(memberId: Long) = norwegianAuthentication.getStatus(memberId)

    private fun String.toTwoLetterLanguage() = when (this) {
        "sv-SE" -> "SV"
        "en-SE",
        "en-NO" -> "EN"
        else -> "NO"
    }
}

