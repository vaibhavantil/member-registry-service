package com.hedvig.memberservice.services

import com.hedvig.external.authentication.NorwegianAuthentication
import com.hedvig.external.authentication.dto.NorwegianAuthenticationResponse
import com.hedvig.external.authentication.dto.NorwegianBankIdAuthenticationRequest
import org.springframework.stereotype.Service
import java.util.*

@Service
class NorwegianBankIdService(
    private val norwegianAuthentication: NorwegianAuthentication
) {

    fun authenticate(request: NorwegianBankIdAuthenticationRequest): NorwegianAuthenticationResponse {
        val response = norwegianAuthentication.auth(request)

        startCollect(response.id)
        return response
    }

    fun sign(request: NorwegianBankIdAuthenticationRequest): NorwegianAuthenticationResponse {
        val response = norwegianAuthentication.sign(request)

        startCollect(response.id)
        return response
    }

    private fun startCollect(reference: UUID) {
        TODO("start job publishing to norwegianAuthentication.collect(reference) to redis")
    }
}
