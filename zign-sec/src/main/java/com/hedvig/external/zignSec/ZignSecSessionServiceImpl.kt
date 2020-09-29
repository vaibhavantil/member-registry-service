package com.hedvig.external.zignSec

import com.fasterxml.jackson.databind.ObjectMapper
import com.hedvig.external.authentication.dto.ZignSecAuthenticationResult
import com.hedvig.external.authentication.dto.ZignSecSignResult
import com.hedvig.external.authentication.dto.StartZignSecAuthenticationResult
import com.hedvig.external.authentication.dto.ZignSecAuthenticationResponseError
import com.hedvig.external.authentication.dto.ZignSecBankIdAuthenticationRequest
import com.hedvig.external.authentication.dto.ZignSecBankIdProgressStatus
import com.hedvig.external.event.AuthenticationEventPublisher
import com.hedvig.external.zignSec.client.dto.ZignSecCollectState
import com.hedvig.external.zignSec.client.dto.ZignSecNotificationRequest
import com.hedvig.external.zignSec.repository.ZignSecSessionRepository
import com.hedvig.external.zignSec.repository.ZignSecSignEntityRepository
import com.hedvig.external.zignSec.repository.entitys.ZignSecAuthenticationType
import com.hedvig.external.zignSec.repository.entitys.ZignSecNotification
import com.hedvig.external.zignSec.repository.entitys.ZignSecSession
import com.hedvig.external.zignSec.repository.entitys.ZignSecSignEntity
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class ZignSecSessionServiceImpl(
    private val sessionRepository: ZignSecSessionRepository,
    private val zignSecSignEntityRepository: ZignSecSignEntityRepository,
    private val zignSecService: ZignSecService,
    private val authenticationEventPublisher: AuthenticationEventPublisher,
    private val objectMapper: ObjectMapper
) : ZignSecSessionService {

    override fun auth(request: ZignSecBankIdAuthenticationRequest): StartZignSecAuthenticationResult =
        authenticate(request, ZignSecAuthenticationType.AUTH)

    override fun sign(request: ZignSecBankIdAuthenticationRequest): StartZignSecAuthenticationResult =
        authenticate(request, ZignSecAuthenticationType.SIGN)

    override fun getStatus(memberId: Long): ZignSecBankIdProgressStatus? {
        val optionalSession = sessionRepository.findByMemberId(memberId)

        return if (optionalSession.isPresent) {
            val session = optionalSession.get()
            if (session.requestType == ZignSecAuthenticationType.SIGN) {
                if (session.status == ZignSecBankIdProgressStatus.COMPLETED) {
                    if (session.isContractsCreated) ZignSecBankIdProgressStatus.COMPLETED else ZignSecBankIdProgressStatus.IN_PROGRESS
                } else {
                    optionalSession.get().status
                }
            } else {
                optionalSession.get().status
            }
        } else {
            null
        }
    }

    override fun notifyContractsCreated(memberId: Long) {
        val session = sessionRepository.findByMemberId(memberId).get()
        session.isContractsCreated = true
        sessionRepository.save(session)
    }

    private fun authenticate(request: ZignSecBankIdAuthenticationRequest, type: ZignSecAuthenticationType): StartZignSecAuthenticationResult {
        val optional = sessionRepository.findByMemberId(request.memberId.toLong())

        return if (optional.isPresent) {
            val session = optional.get()

            if (session.requestType != type) {
                sessionRepository.delete(session)
                return StartZignSecAuthenticationResult.Failed(
                    errors = listOf(
                        ZignSecAuthenticationResponseError(0, "Illegal change of method. Tried to change from type: ${session.requestType} to $type")
                    )
                )
            }

            if (session.requestPersonalNumber != request.personalNumber) {
                return startNewSession(request, type, session)
            }

            when (session.status) {
                ZignSecBankIdProgressStatus.INITIATED,
                ZignSecBankIdProgressStatus.IN_PROGRESS -> {
                    val collectResponse = try {
                        zignSecService.collect(session.referenceId)
                    } catch (e: Exception) {
                        return startNewSession(request, type, session)
                    }
                    when (collectResponse.result.identity.state) {
                        ZignSecCollectState.PENDING -> StartZignSecAuthenticationResult.Success(
                            session.referenceId,
                            session.redirectUrl.trim()
                        )
                        ZignSecCollectState.FINISHED -> startNewSession(request, type, session)
                    }
                }
                ZignSecBankIdProgressStatus.FAILED,
                ZignSecBankIdProgressStatus.COMPLETED -> startNewSession(request, type, session)
            }
        } else {
            startNewSession(request, type, null)
        }
    }

    private fun startNewSession(request: ZignSecBankIdAuthenticationRequest, type: ZignSecAuthenticationType, session: ZignSecSession?): StartZignSecAuthenticationResult {
        val response = zignSecService.auth(request)

        if (response.errors.isNotEmpty() || response.redirectUrl == null) {
            val errors = response.errors.map { ZignSecAuthenticationResponseError(it.code, it.description) }.toMutableList()

            if (errors.isEmpty() && response.redirectUrl == null) {
                errors.add(ZignSecAuthenticationResponseError(
                    -1,
                    "No errors and no redirect error from ZignSec"
                ))
            }

            return StartZignSecAuthenticationResult.Failed(
                errors
            )
        }

        val s = session?.apply {
            this.referenceId = response.id
            this.redirectUrl = response.redirectUrl.trim()
            this.requestPersonalNumber = request.personalNumber
            this.status = ZignSecBankIdProgressStatus.INITIATED
        } ?: ZignSecSession(
            memberId = request.memberId.toLong(),
            requestType = type,
            referenceId = response.id,
            redirectUrl = response.redirectUrl.trim(),
            requestPersonalNumber = request.personalNumber,
            authenticationMethod = request.authMethod
        )

        sessionRepository.save(s)

        return StartZignSecAuthenticationResult.Success(
            response.id,
            response.redirectUrl.trim()
        )
    }

    override fun handleNotification(jsonRequest: String) {
        val request = objectMapper.readValue(jsonRequest, ZignSecNotificationRequest::class.java)
        val session = sessionRepository.findByReferenceId(request.id).get()

        when (session.status) {
            ZignSecBankIdProgressStatus.INITIATED,
            ZignSecBankIdProgressStatus.IN_PROGRESS -> updateSession(session, request, jsonRequest)
            ZignSecBankIdProgressStatus.FAILED,
            ZignSecBankIdProgressStatus.COMPLETED -> {
                if (session.status != getSessionStatusFromNotification(request)) {
                    logger.error("ZignSec webhook notification is trying to change status on session that is failed or completed [Session: $session] [Request: $request]")
                }
            }
        }
    }

    private fun updateSession(session: ZignSecSession, request: ZignSecNotificationRequest, jsonRequest: String) {
        val notification = ZignSecNotification.from(request)
        session.notification = notification

        session.status = getSessionStatusFromNotification(request)

        when (session.requestType) {
            ZignSecAuthenticationType.SIGN -> {
                when (session.status) {
                    ZignSecBankIdProgressStatus.INITIATED,
                    ZignSecBankIdProgressStatus.IN_PROGRESS -> { /* strange but no-op */
                    }
                    ZignSecBankIdProgressStatus.FAILED -> authenticationEventPublisher.publishSignEvent(
                        ZignSecSignResult.Failed(
                            session.referenceId,
                            session.memberId,
                            session.authenticationMethod
                        )
                    )
                    ZignSecBankIdProgressStatus.COMPLETED -> {
                        //TODO: re add this when everything is in order with zign sec and person number also un ignore the test in ZignSecSessionServiceImplTest
                        //NOTE: this is also used in denmark so make sure both work on changing
                        //check that personal number is matching when signing
//                        if (notification.identity!!.personalNumber!! != session.requestPersonalNumber!!) {
                        if (notification.identity!!.dateOfBirth!!.dayMonthAndTwoDigitYearFromDateOfBirth() != session.requestPersonalNumber!!.dayMonthAndTwoDigitYearFromNorwegianOrDanishSsn()) {
                            session.status = ZignSecBankIdProgressStatus.FAILED
                            authenticationEventPublisher.publishSignEvent(
                                ZignSecSignResult.Failed(
                                    session.referenceId,
                                    session.memberId,
                                    session.authenticationMethod
                                )
                            )
                        } else {
                            memberSigned(session, jsonRequest)
                        }
                    }
                }
            }
            ZignSecAuthenticationType.AUTH -> {
                when (session.status) {
                    ZignSecBankIdProgressStatus.INITIATED,
                    ZignSecBankIdProgressStatus.IN_PROGRESS -> { /* strange but no-op */
                    }
                    ZignSecBankIdProgressStatus.FAILED -> authenticationEventPublisher.publishAuthenticationEvent(
                        ZignSecAuthenticationResult.Failed(
                            session.referenceId,
                            session.memberId))
                    ZignSecBankIdProgressStatus.COMPLETED -> {
                        val idProviderPersonId = session.notification!!.identity!!.idProviderPersonId!!

                        val signEntity = zignSecSignEntityRepository.findByIdProviderPersonId(idProviderPersonId)

                        if (signEntity != null) {
                            authenticationEventPublisher.publishAuthenticationEvent(
                                ZignSecAuthenticationResult.Completed(
                                    session.referenceId,
                                    session.memberId,
                                    signEntity.personalNumber
                                )
                            )
                        } else {
                            logger.error("Member tried to login whit no ZignSecSignEntity [MemberId:${session.memberId}] [idProviderPersonId: $idProviderPersonId] [SessionId:${session.sessionId}] [session:$session]")
                            authenticationEventPublisher.publishAuthenticationEvent(
                                ZignSecAuthenticationResult.Failed(
                                    session.referenceId,
                                    session.memberId
                                )
                            )
                        }
                    }
                }
            }
        }

        sessionRepository.save(session)
    }

    private fun memberSigned(session: ZignSecSession, jsonRequest: String) {
        assert(!session.requestPersonalNumber.isNullOrEmpty() && !session.notification?.identity?.idProviderPersonId.isNullOrEmpty()) {
            authenticationEventPublisher.publishSignEvent(
                ZignSecSignResult.Failed(
                    session.referenceId,
                    session.memberId,
                    session.authenticationMethod
                )
            )
            return@assert "Must have requestPersonalNumber on session to sign member"
        }

        val signEntity = ZignSecSignEntity(
            personalNumber = session.requestPersonalNumber!!,
            idProviderPersonId = session.notification!!.identity!!.idProviderPersonId!!
        )
        zignSecSignEntityRepository.save(signEntity)
        authenticationEventPublisher.publishSignEvent(
            ZignSecSignResult.Signed(
                session.referenceId,
                session.memberId,
                session.requestPersonalNumber!!,
                jsonRequest,
                session.authenticationMethod
            )
        )
    }

    private fun getSessionStatusFromNotification(request: ZignSecNotificationRequest) = if (request.errors.isEmpty()) {
        ZignSecBankIdProgressStatus.COMPLETED
    } else {
        ZignSecBankIdProgressStatus.FAILED
    }

    companion object {
        private val logger = LoggerFactory.getLogger(ZignSecServiceImpl::class.java)
    }
}

fun String.dayMonthAndTwoDigitYearFromNorwegianOrDanishSsn(): Triple<String, String, String> {
    val trimmedInput = this.trim().replace("-", "").replace(" ", "")
    val day = trimmedInput.substring(0, 2)
    val month = trimmedInput.substring(2, 4)
    val twoDigitYear = trimmedInput.substring(4, 6)
    return Triple(day, month, twoDigitYear)
}

//Format `yyyy-mm-dd`
fun String.dayMonthAndTwoDigitYearFromDateOfBirth(): Triple<String, String, String> {
    val twoDigitYear = this.substring(2, 4)
    val month = this.substring(5, 7)
    val day = this.substring(8, 10)
    return Triple(day, month, twoDigitYear)
}
