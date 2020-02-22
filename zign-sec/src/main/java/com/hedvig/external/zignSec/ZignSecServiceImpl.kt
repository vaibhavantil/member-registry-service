package com.hedvig.external.zignSec

import com.hedvig.external.authentication.dto.NorwegianBankIdAuthenticationRequest
import com.hedvig.external.zignSec.client.ZignSecClient
import com.hedvig.external.zignSec.client.dto.ZignSecRequestBody
import com.hedvig.external.zignSec.client.dto.ZignSecResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class ZignSecServiceImpl(
    private val client: ZignSecClient,
    @Value("\${zignsec.authentication.token:authenticate-me}")
    private val authentication: String,
    @Value("\${zignsec.host:test.zignsec.com}")
    private val host: String,
    @Value("\${zignsec.webhook.url}")
    private val webhookUrl: String,
    @Value("\${zignsec.success.url}")
    private val sucessUrl: String,
    @Value("\${zignsec.failed.url}")
    private val failedUrl: String
): ZignSecService {

    override fun auth(request: NorwegianBankIdAuthenticationRequest): ZignSecResponse = client.auth(
        bankIdSelector = WEB_OR_MOBILE,
        authorization = authentication,
        host = host,
        body = ZignSecRequestBody(
            personalnumber = request.personalNumber,
            language = request.language,
            target = sucessUrl,
            targetError = failedUrl,
            webhook = webhookUrl
        )
    )

    companion object {
        private const val WEB_OR_MOBILE = "nbid_oidc"
    }
}
