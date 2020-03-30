package com.hedvig.external.zignSec

import com.fasterxml.jackson.databind.ObjectMapper
import com.hedvig.external.authentication.dto.NorwegianAuthenticationResult
import com.hedvig.external.authentication.dto.NorwegianSignResult
import com.hedvig.external.authentication.dto.StartNorwegianAuthenticationResult
import com.hedvig.external.authentication.dto.NorwegianAuthenticationResponseError
import com.hedvig.external.authentication.dto.NorwegianBankIdAuthenticationRequest
import com.hedvig.external.authentication.dto.NorwegianBankIdProgressStatus
import com.hedvig.external.event.NorwegianAuthenticationEventPublisher
import com.hedvig.external.zignSec.client.dto.ZignSecCollectState
import com.hedvig.external.zignSec.client.dto.ZignSecNotificationRequest
import com.hedvig.external.zignSec.repository.ZignSecSessionRepository
import com.hedvig.external.zignSec.repository.entitys.NorwegianAuthenticationType
import com.hedvig.external.zignSec.repository.entitys.ZignSecNotification
import com.hedvig.external.zignSec.repository.entitys.ZignSecSession
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

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

    override fun getStatus(memberId: Long): NorwegianBankIdProgressStatus? {
        val optionalSession = sessionRepository.findByMemberId(memberId)

        return if (optionalSession.isPresent) {
            optionalSession.get().status
        } else {
            null
        }
    }

    private fun authenticate(request: NorwegianBankIdAuthenticationRequest, type: NorwegianAuthenticationType): StartNorwegianAuthenticationResult {
        val optional = sessionRepository.findByMemberId(request.memberId.toLong())

        return if (optional.isPresent) {
            val session = optional.get()

            if (session.requestType != type){
                sessionRepository.delete(session)
                return StartNorwegianAuthenticationResult.Failed(
                    errors = listOf(
                        NorwegianAuthenticationResponseError(0, "Illegal change of method. Tried to change from type: ${session.requestType} to $type")
                    )
                )
            }

            if (session.personalNumber != request.personalNumber){
                return startNewSession(request, type, session)
            }

            when (session.status) {
                NorwegianBankIdProgressStatus.INITIATED,
                NorwegianBankIdProgressStatus.IN_PROGRESS -> {
                    val collectResponse = try {
                         zignSecService.collect(session.referenceId)
                    } catch (e: Exception) {
                        return startNewSession(request, type, session)
                    }
                    when (collectResponse.result.identity.state) {
                        ZignSecCollectState.PENDING -> StartNorwegianAuthenticationResult.Success(
                            session.referenceId,
                            session.redirectUrl
                        )
                        ZignSecCollectState.FINISHED -> startNewSession(request, type, session)
                    }
                }
                NorwegianBankIdProgressStatus.FAILED,
                NorwegianBankIdProgressStatus.COMPLETED -> startNewSession(request, type, session)
            }
        } else {
            startNewSession(request, type, null)
        }
    }

    private fun startNewSession(request: NorwegianBankIdAuthenticationRequest, type: NorwegianAuthenticationType, session: ZignSecSession?): StartNorwegianAuthenticationResult {
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
                errors
            )
        }

        val s = session?.apply {
            this.referenceId = response.id
            this.redirectUrl = response.redirectUrl
            this.personalNumber = request.personalNumber
        } ?: ZignSecSession(
            memberId = request.memberId.toLong(),
            requestType = type,
            referenceId = response.id,
            redirectUrl = response.redirectUrl,
            personalNumber = request.personalNumber
        )

        sessionRepository.save(s)

        return StartNorwegianAuthenticationResult.Success(
            response.id,
            response.redirectUrl
        )
    }

    override fun handleNotification(jsonRequest: String) {
        val request = objectMapper.readValue(jsonRequest, ZignSecNotificationRequest::class.java)
        val session = sessionRepository.findByReferenceId(request.id).get()

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
                            session.referenceId,
                            session.memberId
                        )
                    )
                    NorwegianBankIdProgressStatus.COMPLETED -> norwegianAuthenticationEventPublisher.publishSignEvent(
                        NorwegianSignResult.Signed(
                            session.referenceId,
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
                            session.referenceId,
                            session.memberId))
                    NorwegianBankIdProgressStatus.COMPLETED -> norwegianAuthenticationEventPublisher.publishAuthenticationEvent(
                        NorwegianAuthenticationResult.Completed(
                            session.referenceId, session.memberId,
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
