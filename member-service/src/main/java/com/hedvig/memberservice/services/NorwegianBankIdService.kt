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

    fun sign(memberId: String, ssn: String, acceptLanguage: String, isMobile: Boolean) =
        norwegianAuthentication.sign(
            NorwegianBankIdAuthenticationRequest(
                memberId,
                ssn,
                acceptLanguage.toTwoLetterLanguage(),
                isMobile
            )
        )

    //TODO: maybe we should map more
    private fun String.toTwoLetterLanguage() = when (this) {
        "sv-SE" -> "SV"
        "en-SE",
        "en-NO" -> "EN"
        else -> "NO"
    }
}

