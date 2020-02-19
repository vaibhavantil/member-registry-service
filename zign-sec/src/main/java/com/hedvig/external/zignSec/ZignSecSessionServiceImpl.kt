package com.hedvig.external.zignSec

import com.hedvig.external.authentication.dto.NorwegianAuthenticationCollectResponse
import com.hedvig.external.authentication.dto.NorwegianAuthenticationResponse
import com.hedvig.external.authentication.dto.NorwegianAuthenticationResponseError
import com.hedvig.external.authentication.dto.NorwegianBankIdAuthenticationRequest
import com.hedvig.external.authentication.dto.NorwegianBankIdProgressStatus
import com.hedvig.external.event.NorwegianAuthenticationEvent
import com.hedvig.external.event.NorwegianAuthenticationEventPublisher
import com.hedvig.external.event.NorwegianSignEvent
import com.hedvig.external.zignSec.client.ZignSecClient
import com.hedvig.external.zignSec.client.dto.ZignSecNotificationRequest
import com.hedvig.external.zignSec.client.dto.ZignSecRequestBody
import com.hedvig.external.zignSec.repository.ZignSecSessionRepository
import com.hedvig.external.zignSec.repository.entitys.NorwegianAuthenticationType
import com.hedvig.external.zignSec.repository.entitys.ZignSecNotification
import com.hedvig.external.zignSec.repository.entitys.ZignSecSession
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import java.util.*

@Service
class ZignSecSessionServiceImpl(
    private val sessionRepository: ZignSecSessionRepository,
    private val zignSecService: ZignSecService,
    private val norwegianAuthenticationEventPublisher: NorwegianAuthenticationEventPublisher
) : ZignSecSessionService {

    override fun auth(request: NorwegianBankIdAuthenticationRequest): NorwegianAuthenticationResponse =
        authenticate(request, NorwegianAuthenticationType.AUTH)

    override fun sign(request: NorwegianBankIdAuthenticationRequest): NorwegianAuthenticationResponse =
        authenticate(request, NorwegianAuthenticationType.SIGN)

    private fun authenticate(request: NorwegianBankIdAuthenticationRequest, type: NorwegianAuthenticationType): NorwegianAuthenticationResponse {
        val response = zignSecService.auth(request)

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

        when (session.requestType) {
            NorwegianAuthenticationType.SIGN ->
                norwegianAuthenticationEventPublisher.publishSignEvent(NorwegianAuthenticationCollectResponse(session.status))
            NorwegianAuthenticationType.AUTH ->
                norwegianAuthenticationEventPublisher.publishAuthenticationEvent(NorwegianAuthenticationCollectResponse(session.status))
        }

        sessionRepository.save(session)
    }

}
