package com.hedvig.external.authentication

import com.hedvig.external.authentication.dto.NorwegianAuthenticationCollectResponse
import com.hedvig.external.authentication.dto.NorwegianBankIdAuthenticationRequest
import com.hedvig.external.authentication.dto.NorwegianAuthenticationResponse
import com.hedvig.external.authentication.dto.NorwegianAuthenticationResponseError
import com.hedvig.external.zignSec.client.ZignSecClient
import com.hedvig.external.zignSec.client.dto.ZignSecRequestBody
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.util.*

@Service
class NorwegianAuthenticationImpl(
    private val zignSecClient: ZignSecClient
): NorwegianAuthentication {

    @Value("\${zignsec.authentication.token}")
    private lateinit var authentication: String

    @Value("\${zignsec.host:test.zignsec.com}")
    private lateinit var host: String

    override fun auth(request: NorwegianBankIdAuthenticationRequest): NorwegianAuthenticationResponse {
        val body = ZignSecRequestBody(
            personalnumber = request.personalNumber,
            language = request.language
        )
        val response = zignSecClient.auth(
            bankIdSelector = if(request.isMobile) BANK_ID_MOBILE else BANK_ID_WEB,
            authorization = authentication,
            host = host,
            body = body
        )

        return NorwegianAuthenticationResponse(
            response.id,
            response.errors.map { NorwegianAuthenticationResponseError(it.code, it.description) },
            response.redirectUrl
        )
    }

    override fun sign(request: NorwegianBankIdAuthenticationRequest): NorwegianAuthenticationResponse {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun collect(reference: UUID): NorwegianAuthenticationCollectResponse {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    companion object {
        private const val BANK_ID_WEB = "nbid"
        private const val BANK_ID_MOBILE = "nbid_mobile"
    }
}
