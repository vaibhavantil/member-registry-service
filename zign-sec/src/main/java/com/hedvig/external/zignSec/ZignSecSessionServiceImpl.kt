package com.hedvig.external.zignSec

import com.hedvig.external.authentication.dto.NorwegianAuthenticationCollectResponse
import com.hedvig.external.authentication.dto.StartNorwegianAuthenticationResult
import com.hedvig.external.authentication.dto.NorwegianAuthenticationResponseError
import com.hedvig.external.authentication.dto.NorwegianBankIdAuthenticationRequest
import com.hedvig.external.authentication.dto.NorwegianBankIdProgressStatus
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
    private val zignSecService: ZignSecService
) : ZignSecSessionService {

    override fun auth(request: NorwegianBankIdAuthenticationRequest): StartNorwegianAuthenticationResult =
        authenticate(request, NorwegianAuthenticationType.AUTH)

    override fun sign(request: NorwegianBankIdAuthenticationRequest): StartNorwegianAuthenticationResult =
        authenticate(request, NorwegianAuthenticationType.SIGN)

    private fun authenticate(request: NorwegianBankIdAuthenticationRequest, type: NorwegianAuthenticationType): StartNorwegianAuthenticationResult {
        val response = zignSecService.auth(request)

        if (response.errors.isNotEmpty()) {
            return StartNorwegianAuthenticationResult.Failed (
                response.id,
                response.errors.map { NorwegianAuthenticationResponseError(it.code, it.description) }
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

    override fun collect(sessionId: UUID): NorwegianAuthenticationCollectResponse {
        val session = sessionRepository.findById(sessionId).get()

        return NorwegianAuthenticationCollectResponse(
            status = session.status
        )
    }

    override fun handleNotification(request: ZignSecNotificationRequest) {
        val session = sessionRepository.findById(request.id).get()

        when (session.status) {
            NorwegianBankIdProgressStatus.INITIATED,
            NorwegianBankIdProgressStatus.IN_PROGRESS -> updateSession(session, request)
            NorwegianBankIdProgressStatus.FAILED,
            NorwegianBankIdProgressStatus.COMPLETED -> {
                if (session.status != getSessionStatusFromNotification(request)) {
                    logger.error("ZignSec webhook notification is trying to change status on session that is failed or completed [Session: $session] [Request: $request]")
                }
            }
        }
    }

    private fun updateSession(session: ZignSecSession, request: ZignSecNotificationRequest) {
        session.notification = ZignSecNotification.from(request)

        session.status = getSessionStatusFromNotification(request)

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
