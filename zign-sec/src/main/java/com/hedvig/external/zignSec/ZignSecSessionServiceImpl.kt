package com.hedvig.external.zignSec

import com.fasterxml.jackson.databind.ObjectMapper
import com.hedvig.external.authentication.dto.NorwegianAuthenticationResult
import com.hedvig.external.authentication.dto.NorwegianSignResult
import com.hedvig.external.authentication.dto.StartNorwegianAuthenticationResult
import com.hedvig.external.authentication.dto.NorwegianAuthenticationResponseError
import com.hedvig.external.authentication.dto.NorwegianBankIdAuthenticationRequest
import com.hedvig.external.authentication.dto.NorwegianBankIdProgressStatus
import com.hedvig.external.event.NorwegianAuthenticationEventPublisher
import com.hedvig.external.zignSec.client.dto.ZignSecNotificationRequest
import com.hedvig.external.zignSec.repository.ZignSecSessionRepository
import com.hedvig.external.zignSec.repository.entitys.NorwegianAuthenticationType
import com.hedvig.external.zignSec.repository.entitys.ZignSecNotification
import com.hedvig.external.zignSec.repository.entitys.ZignSecSession
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.util.*

@Service
class ZignSecSessionServiceImpl(
    private val sessionRepository: ZignSecSessionRepository,
    private val zignSecService: ZignSecService,
    private val norwegianAuthenticationEventPublisher: NorwegianAuthenticationEventPublisher,
    private val objectMapper: ObjectMapper
) : ZignSecSessionService {

    override fun auth(request: NorwegianBankIdAuthenticationRequest): StartNorwegianAuthenticationResult =
        authenticate(request, NorwegianAuthenticationType.AUTH)

    override fun sign(request: NorwegianBankIdAuthenticationRequest): StartNorwegianAuthenticationResult =
        authenticate(request, NorwegianAuthenticationType.SIGN)

    private fun authenticate(request: NorwegianBankIdAuthenticationRequest, type: NorwegianAuthenticationType): StartNorwegianAuthenticationResult {
        val response = zignSecService.auth(request)

        if (response.errors.isNotEmpty() || response.redirectUrl == null) {
            val errors = response.errors.map { NorwegianAuthenticationResponseError(it.code, it.description) }.toMutableList()

            if (errors.isEmpty() && response.redirectUrl == null) {
                errors.add(NorwegianAuthenticationResponseError(
                    -1,
                    "No errors and no redirect error from ZignSec"
                ))
            }

            return StartNorwegianAuthenticationResult.Failed(
                response.id,
                errors
            )
        }

        val session = ZignSecSession(
            memberId = request.memberId.toLong(),
            requestType = type
        )

        sessionRepository.save(session)

        return StartNorwegianAuthenticationResult.Success(
            response.id,
            response.redirectUrl
        )
    }

    override fun handleNotification(jsonRequest: String) {
        val request = objectMapper.readValue(jsonRequest, ZignSecNotificationRequest::class.java)
        val session = sessionRepository.findById(request.id).get()

        when (session.status) {
            NorwegianBankIdProgressStatus.INITIATED,
            NorwegianBankIdProgressStatus.IN_PROGRESS -> updateSession(session, request, jsonRequest)
            NorwegianBankIdProgressStatus.FAILED,
            NorwegianBankIdProgressStatus.COMPLETED -> {
                if (session.status != getSessionStatusFromNotification(request)) {
                    logger.error("ZignSec webhook notification is trying to change status on session that is failed or completed [Session: $session] [Request: $request]")
                }
            }
        }
    }

    private fun updateSession(session: ZignSecSession, request: ZignSecNotificationRequest, jsonRequest: String) {
        session.notification = ZignSecNotification.from(request)

        session.status = getSessionStatusFromNotification(request)

        when (session.requestType) {
            NorwegianAuthenticationType.SIGN -> {
                when (session.status) {
                    NorwegianBankIdProgressStatus.INITIATED,
                    NorwegianBankIdProgressStatus.IN_PROGRESS -> { /* strange but no-op */ }
                    NorwegianBankIdProgressStatus.FAILED -> norwegianAuthenticationEventPublisher.publishSignEvent(
                        NorwegianSignResult.Failed(
                            session.sessionId,
                            session.memberId
                        )
                    )
                    NorwegianBankIdProgressStatus.COMPLETED -> norwegianAuthenticationEventPublisher.publishSignEvent(
                        NorwegianSignResult.Signed(
                            session.sessionId,
                            session.memberId,
                            session.notification!!.identity!!.personalNumber!!,
                            jsonRequest
                        )
                    )
                }
            }
            NorwegianAuthenticationType.AUTH -> {
                when (session.status) {
                    NorwegianBankIdProgressStatus.INITIATED,
                    NorwegianBankIdProgressStatus.IN_PROGRESS -> { /* strange but no-op */ }
                    NorwegianBankIdProgressStatus.FAILED -> norwegianAuthenticationEventPublisher.publishAuthenticationEvent(
                        NorwegianAuthenticationResult.Failed(
                            session.sessionId,
                            session.memberId))
                    NorwegianBankIdProgressStatus.COMPLETED -> norwegianAuthenticationEventPublisher.publishAuthenticationEvent(
                        NorwegianAuthenticationResult.Completed(
                            session.sessionId, session.memberId,
                            session.notification!!.identity!!.personalNumber!!)
                    )
                }
            }
        }

        sessionRepository.save(session)
    }

    private fun getSessionStatusFromNotification(request: ZignSecNotificationRequest) = if (request.errors.isEmpty()) {
        NorwegianBankIdProgressStatus.COMPLETED
    } else {
        NorwegianBankIdProgressStatus.FAILED
    }

    companion object {
        private val logger = LoggerFactory.getLogger(ZignSecServiceImpl::class.java)
    }
}
