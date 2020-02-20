package com.hedvig.memberservice.services

import com.hedvig.external.authentication.NorwegianAuthentication
import com.hedvig.external.authentication.dto.StartNorwegianAuthenticationResult
import com.hedvig.external.authentication.dto.NorwegianBankIdAuthenticationRequest
import org.springframework.stereotype.Service
import java.util.*

@Service
class NorwegianBankIdService(
    private val norwegianAuthentication: NorwegianAuthentication
) {

    fun authenticate(request: NorwegianBankIdAuthenticationRequest): StartNorwegianAuthenticationResult {
        val response = norwegianAuthentication.auth(request)

        //startCollect(response.id) will change this in an later PR
        return response
    }

    fun sign(memberId: String, ssn: String, acceptLanguage: String, isMobile: Boolean): StartNorwegianAuthenticationResult {
        val response = norwegianAuthentication.sign(
            NorwegianBankIdAuthenticationRequest(
                memberId,
                ssn,
                acceptLanguage.toTwoLetterLanguage(),
                isMobile
            )
        )

        //startCollect(response.id) will change this in an later PR
        return response
    }

    //TODO: maybe we should map more
    private fun String.toTwoLetterLanguage() = when (this) {
        "sv-SE" -> "SV"
        "en-SE" -> "EN"
        else -> "NO"
    }

    private fun startCollect(reference: UUID) {
        TODO("start job publishing to norwegianAuthentication.collect(reference) to redis")
    }
}

