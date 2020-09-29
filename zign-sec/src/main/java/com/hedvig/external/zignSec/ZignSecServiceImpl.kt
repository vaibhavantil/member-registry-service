package com.hedvig.external.zignSec

import com.hedvig.external.authentication.dto.ZignSecBankIdAuthenticationRequest
import com.hedvig.external.zignSec.client.ZignSecClient
import com.hedvig.external.zignSec.client.dto.ZignSecRequestBody
import com.hedvig.external.zignSec.client.dto.ZignSecResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.util.*


@Service
class ZignSecServiceImpl(
    private val client: ZignSecClient,
    @Value("\${zignsec.authentication.token:authenticate-me}")
    private val authentication: String,
    @Value("\${zignsec.host}")
    private val host: String,
    @Value("\${zignsec.webhook.url}")
    private val webhookUrl: String
) : ZignSecService {

    override fun auth(request: ZignSecBankIdAuthenticationRequest): ZignSecResponse = client.auth(
        bankIdSelector = request.authMethod.zignSecMethodName,
        authorization = authentication,
        host = host,
        body = ZignSecRequestBody(
            personalnumber = request.personalNumber,
            language = request.language,
            target = request.successUrl,
            targetError = request.failUrl,
            webhook = webhookUrl
        )
    )

    override fun collect(referenceId: UUID) = client.collect(
        sessionId = referenceId,
        authorization = authentication,
        host = host
    )
}
