package com.hedvig.external.zignSec

import com.hedvig.external.authentication.dto.NorwegianAuthenticationCollectResponse
import com.hedvig.external.authentication.dto.NorwegianAuthenticationResponse
import com.hedvig.external.authentication.dto.NorwegianAuthenticationResponseError
import com.hedvig.external.authentication.dto.NorwegianBankIdAuthenticationRequest
import com.hedvig.external.authentication.dto.NorwegianBankIdProgressStatus
import com.hedvig.external.zignSec.client.ZignSecClient
import com.hedvig.external.zignSec.client.dto.ZignSecNotificationRequest
import com.hedvig.external.zignSec.client.dto.ZignSecRequestBody
import com.hedvig.external.zignSec.repository.ZignSecSessionRepository
import com.hedvig.external.zignSec.repository.entitys.NorwegianAuthenticationType
import com.hedvig.external.zignSec.repository.entitys.ZignSecNotification
import com.hedvig.external.zignSec.repository.entitys.ZignSecSession
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.util.*

@Service
class ZignSecServiceImpl(
    private val sessionRepository: ZignSecSessionRepository,
    private val client: ZignSecClient,
    @Value("\${zignsec.authentication.token:authenticate-me}")
    val authentication: String,
    @Value("\${zignsec.host:test.zignsec.com}")
    val host: String
) : ZignSecService {

    override fun auth(request: NorwegianBankIdAuthenticationRequest): NorwegianAuthenticationResponse =
        authenticate(request, NorwegianAuthenticationType.AUTH)

    override fun sign(request: NorwegianBankIdAuthenticationRequest): NorwegianAuthenticationResponse =
        authenticate(request, NorwegianAuthenticationType.SIGN)

    private fun authenticate(request: NorwegianBankIdAuthenticationRequest, type: NorwegianAuthenticationType): NorwegianAuthenticationResponse {
        val body = ZignSecRequestBody(
            personalnumber = request.personalNumber,
            language = request.language,
            webhook = request.webhook
        )
        val response = client.auth(
            bankIdSelector = if (request.isMobile) BANK_ID_MOBILE else BANK_ID_WEB,
            authorization = authentication,
            host = host,
            body = body
        )

        val session = ZignSecSession(
            memberId = request.memberId.toLong(),
            requestType = type
        )

        sessionRepository.save(session)

        return NorwegianAuthenticationResponse(
            response.id,
            response.errors.map { NorwegianAuthenticationResponseError(it.code, it.description) },
            response.redirectUrl
        )
    }

    override fun collect(sessionId: UUID): NorwegianAuthenticationCollectResponse {
        val session = sessionRepository.findById(sessionId).get()

        return NorwegianAuthenticationCollectResponse(
            status = session.status
        )
    }

    override fun handleNotification(request: ZignSecNotificationRequest) {
        val session = sessionRepository.findById(request.id).get()

        session.notification = ZignSecNotification.from(request)

        if (request.errors.isEmpty()) {
            session.status = NorwegianBankIdProgressStatus.COMPLETED
        } else {
            session.status = NorwegianBankIdProgressStatus.FAILED
        }

        sessionRepository.save(session)
    }

    companion object {
        private const val BANK_ID_WEB = "nbid"
        private const val BANK_ID_MOBILE = "nbid_mobile"
    }
}
