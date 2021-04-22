package com.hedvig.external.zignSec

import com.fasterxml.jackson.databind.ObjectMapper
import com.hedvig.external.authentication.dto.ZignSecAuthenticationResult
import com.hedvig.external.authentication.dto.ZignSecSignResult
import com.hedvig.external.authentication.dto.StartZignSecAuthenticationResult
import com.hedvig.external.authentication.dto.ZignSecAuthenticationMethod
import com.hedvig.external.authentication.dto.ZignSecAuthenticationResponseError
import com.hedvig.external.authentication.dto.ZignSecBankIdAuthenticationRequest
import com.hedvig.external.authentication.dto.ZignSecBankIdProgressStatus
import com.hedvig.external.event.AuthenticationEventPublisher
import com.hedvig.external.zignSec.client.dto.ZignSecCollectState
import com.hedvig.external.zignSec.client.dto.ZignSecNotificationRequest
import com.hedvig.external.zignSec.repository.ZignSecSessionRepository
import com.hedvig.external.zignSec.repository.ZignSecAuthenticationEntityRepository
import com.hedvig.external.zignSec.repository.entitys.ZignSecAuthenticationType
import com.hedvig.external.zignSec.repository.entitys.ZignSecNotification
import com.hedvig.external.zignSec.repository.entitys.ZignSecSession
import com.hedvig.external.zignSec.repository.entitys.ZignSecAuthenticationEntity
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.text.DecimalFormat

@Service
class ZignSecSessionServiceImpl(
    private val sessionRepository: ZignSecSessionRepository,
    private val zignSecAuthenticationEntityRepository: ZignSecAuthenticationEntityRepository,
    private val zignSecService: ZignSecService,
    private val authenticationEventPublisher: AuthenticationEventPublisher,
    private val objectMapper: ObjectMapper,
    private val metrics: Metrics
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

            if (
                session.requestPersonalNumber != request.personalNumber ||
                session.authenticationMethod != request.authMethod
            ) {
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
        metrics.authStartSession(request, type)

        val response = zignSecService.auth(request)

        if (response.errors.isNotEmpty() || response.redirectUrl == null) {
            val errors = response.errors.map { ZignSecAuthenticationResponseError(it.code, it.description) }.toMutableList()

            if (errors.isEmpty() && response.redirectUrl == null) {
                errors.add(ZignSecAuthenticationResponseError(
                    -1,
                    "No errors and no redirect error from ZignSec"
                ))
            }

            metrics.authRequestFailed(request, type)
            return StartZignSecAuthenticationResult.Failed(
                errors
            )
        }

        val s = session?.apply {
            this.referenceId = response.id
            this.redirectUrl = response.redirectUrl.trim()
            this.requestPersonalNumber = request.personalNumber
            this.status = ZignSecBankIdProgressStatus.INITIATED
            this.authenticationMethod = request.authMethod
        } ?: ZignSecSession(
            memberId = request.memberId.toLong(),
            requestType = type,
            referenceId = response.id,
            redirectUrl = response.redirectUrl.trim(),
            requestPersonalNumber = request.personalNumber,
            authenticationMethod = request.authMethod
        )

        sessionRepository.save(s)

        metrics.authRequestSuccess(request.authMethod, type)
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

        logger.info("Update session of type ${session.requestType} to status: ${session.status}")

        when (session.requestType) {
            ZignSecAuthenticationType.SIGN -> {
                when (session.status) {
                    ZignSecBankIdProgressStatus.INITIATED,
                    ZignSecBankIdProgressStatus.IN_PROGRESS -> { /* strange but no-op */
                    }
                    ZignSecBankIdProgressStatus.FAILED -> {
                        metrics.authSessionFailed(session.authenticationMethod, session.requestType)
                        authenticationEventPublisher.publishSignEvent(
                            ZignSecSignResult.Failed(
                                session.referenceId,
                                session.memberId,
                                session.authenticationMethod
                            )
                        )
                    }
                    ZignSecBankIdProgressStatus.COMPLETED -> {
                        metrics.authSessionSuccess(session.authenticationMethod, session.requestType)
                        if (validatePersonNumberAgainstDateOfBirth(
                                personNumber = session.requestPersonalNumber!!,
                                dateOfBirth = notification.identity!!.dateOfBirth!!,
                                method = session.authenticationMethod
                            )) {
                            memberSigned(
                                session = session,
                                jsonRequest = jsonRequest,
                                firstName = notification.identity.firstName,
                                lastName = notification.identity.lastName
                            )
                        } else {
                            session.status = ZignSecBankIdProgressStatus.FAILED
                            authenticationEventPublisher.publishSignEvent(
                                ZignSecSignResult.Failed(
                                    session.referenceId,
                                    session.memberId,
                                    session.authenticationMethod
                                )
                            )
                        }
                    }
                }
            }
            ZignSecAuthenticationType.AUTH -> {
                when (session.status) {
                    ZignSecBankIdProgressStatus.INITIATED,
                    ZignSecBankIdProgressStatus.IN_PROGRESS -> { /* strange but no-op */
                    }
                    ZignSecBankIdProgressStatus.FAILED -> {
                        metrics.authSessionFailed(session.authenticationMethod, session.requestType)
                        authenticationEventPublisher.publishAuthenticationEvent(
                            ZignSecAuthenticationResult.Failed(
                                session.referenceId,
                                session.memberId))
                    }
                    ZignSecBankIdProgressStatus.COMPLETED -> {
                        metrics.authSessionSuccess(session.authenticationMethod, session.requestType)

                        val idProviderPersonId = session.notification!!.identity!!.idProviderPersonId!!

                        val authEntity = zignSecAuthenticationEntityRepository.findByIdProviderPersonId(idProviderPersonId)?.also {
                          logger.info("Found exact ID number match on authentication entity")
                        } ?: run {
                            logger.info("ID number mismatch, checking date of birth")
                            val requestPersonalNumber = session.requestPersonalNumber ?: return@run null

                            val personalNumberLooksGood = validatePersonNumberAgainstDateOfBirth(
                                personNumber = requestPersonalNumber,
                                dateOfBirth = notification.identity!!.dateOfBirth!!,
                                method = session.authenticationMethod
                            )

                            if (personalNumberLooksGood) {
                                logger.info("Personal number looks good based on date-of-birth heuristic")
                            } else {
                                logger.info("Personal number looks is too much off - bailing")
                                return@run null
                            }

                            zignSecAuthenticationEntityRepository.save(
                                ZignSecAuthenticationEntity(
                                    personalNumber = requestPersonalNumber,
                                    idProviderPersonId = session.notification!!.identity!!.idProviderPersonId!!
                                )
                            )
                        }

                        if (authEntity != null && request.identity != null) {
                            authenticationEventPublisher.publishAuthenticationEvent(
                                ZignSecAuthenticationResult.Completed(
                                    request.identity,
                                    session.referenceId,
                                    session.memberId,
                                    authEntity.personalNumber,
                                    session.authenticationMethod
                                )
                            )
                        } else {
                            logger.error("Member tried to login with no ZignSecSignEntity and requestPersonalNumber was null [MemberId:${session.memberId}] [idProviderPersonId: $idProviderPersonId] [SessionId:${session.sessionId}] [session:$session]")
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

    private fun memberSigned(session: ZignSecSession, jsonRequest: String, firstName: String?, lastName: String?) {
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

        val signEntity = ZignSecAuthenticationEntity(
            personalNumber = session.requestPersonalNumber!!,
            idProviderPersonId = session.notification!!.identity!!.idProviderPersonId!!
        )
        zignSecAuthenticationEntityRepository.save(signEntity)
        authenticationEventPublisher.publishSignEvent(
            ZignSecSignResult.Signed(
                session.referenceId,
                session.memberId,
                session.requestPersonalNumber!!,
                jsonRequest,
                session.authenticationMethod,
                firstName,
                lastName
            )
        )
    }

    private fun getSessionStatusFromNotification(request: ZignSecNotificationRequest) = if (request.errors.isEmpty()) {
        ZignSecBankIdProgressStatus.COMPLETED
    } else {
        ZignSecBankIdProgressStatus.FAILED
    }

    fun validatePersonNumberAgainstDateOfBirth(personNumber: String, dateOfBirth: String, method: ZignSecAuthenticationMethod): Boolean {
        if (personNumber.dayMonthAndTwoDigitYearFromNorwegianOrDanishSsn() == dateOfBirth.dayMonthAndTwoDigitYearFromDateOfBirth()) {
            return true
        }

        return when (method) {
            ZignSecAuthenticationMethod.NORWAY_WEB_OR_MOBILE ->
                validatePersonNumberAgainstDateOfBirthWithKnownNorwegianBankIdBug(personNumber, dateOfBirth) ||
                    validateDNumberAgainstDateOfBirth(personNumber, dateOfBirth)
            ZignSecAuthenticationMethod.DENMARK -> false
        }
    }

    /**
     * On 26:th of january 2021 we found a bug where norwegian bank id returns the birthdate off by one day
     *
     * Note: To this date this has never changed month so for now this is not something we want to include in this fix
     */
    fun validatePersonNumberAgainstDateOfBirthWithKnownNorwegianBankIdBug(personNumber: String, dateOfBirth: String): Boolean {
        val personNumberTriple = personNumber.dayMonthAndTwoDigitYearFromNorwegianOrDanishSsn()
        val dayMonthAndTwoDigitYearTriple = dateOfBirth.dayMonthAndTwoDigitYearFromDateOfBirth()

        val birthdayIncreasedByOne = DATE_FORMAT.format(dayMonthAndTwoDigitYearTriple.first.toInt() + 1)
        return (
            personNumberTriple.first == birthdayIncreasedByOne &&
                personNumberTriple.second == dayMonthAndTwoDigitYearTriple.second &&
                personNumberTriple.third == dayMonthAndTwoDigitYearTriple.third
            )
    }

    /**
     * https://www.skatteetaten.no/en/person/foreign/norwegian-identification-number/d-number/
     */
    fun validateDNumberAgainstDateOfBirth(personNumber: String, dateOfBirth: String): Boolean {
        val personNumberTriple = personNumber.dayMonthAndTwoDigitYearFromNorwegianOrDanishSsn()

        if (personNumberTriple.first.toInt() < 5) {
            return false
        }
        val firstOfPersonNumberDecreasedBy4 = DATE_FORMAT.format(personNumberTriple.first.toInt() - 4)
        val dayMonthAndTwoDigitYearTriple = dateOfBirth.dayMonthAndTwoDigitYearFromDateOfBirth()

        return (
            firstOfPersonNumberDecreasedBy4 == dayMonthAndTwoDigitYearTriple.first &&
                personNumberTriple.second == dayMonthAndTwoDigitYearTriple.second &&
                personNumberTriple.third == dayMonthAndTwoDigitYearTriple.third
            )
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

    companion object {
        private val logger = LoggerFactory.getLogger(ZignSecServiceImpl::class.java)
        val DATE_FORMAT = DecimalFormat("00");
    }
}
