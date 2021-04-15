package com.hedvig.external.zignSec

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.hedvig.external.authentication.dto.ZignSecAuthenticationResult
import com.hedvig.external.authentication.dto.ZignSecSignResult
import com.hedvig.external.authentication.dto.ZignSecBankIdAuthenticationRequest
import com.hedvig.external.authentication.dto.ZignSecBankIdProgressStatus
import com.hedvig.external.authentication.dto.StartZignSecAuthenticationResult.Success
import com.hedvig.external.authentication.dto.StartZignSecAuthenticationResult.Failed
import com.hedvig.external.authentication.dto.ZignSecAuthenticationMethod
import com.hedvig.external.event.AuthenticationEventPublisher
import com.hedvig.external.zignSec.client.dto.*
import com.hedvig.external.zignSec.repository.ZignSecSessionRepository
import com.hedvig.external.zignSec.repository.ZignSecAuthenticationEntityRepository
import com.hedvig.external.zignSec.repository.entitys.Identity
import com.hedvig.external.zignSec.repository.entitys.ZignSecAuthenticationType
import com.hedvig.external.zignSec.repository.entitys.ZignSecSession
import com.hedvig.external.zignSec.repository.entitys.ZignSecAuthenticationEntity
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Captor
import org.mockito.Mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyZeroInteractions
import org.mockito.Mockito.`when` as whenever
import org.mockito.junit.MockitoJUnitRunner
import java.lang.RuntimeException
import java.time.Instant
import java.time.LocalDateTime
import java.util.*

//TODO: Also test ZignSecAuthenticationMethod.DENMARK
@RunWith(MockitoJUnitRunner::class)
class ZignSecSessionServiceImplTest {

    @Mock
    lateinit var sessionRepository: ZignSecSessionRepository

    @Mock
    lateinit var zignSecAuthenticationEntityRepository: ZignSecAuthenticationEntityRepository

    @Mock
    lateinit var zignSecService: ZignSecService

    @Mock
    lateinit var authenticationEventPublisher: AuthenticationEventPublisher

    @Mock
    lateinit var metrics: Metrics

    @Captor
    lateinit var captor: ArgumentCaptor<ZignSecSession>

    @Captor
    lateinit var secAuthenticationEntityCaptor: ArgumentCaptor<ZignSecAuthenticationEntity>

    lateinit var objectMapper: ObjectMapper

    private lateinit var classUnderTest: ZignSecSessionServiceImpl

    @Before
    fun before() {
        objectMapper = ObjectMapper().registerKotlinModule().registerModule(JavaTimeModule())
        classUnderTest = ZignSecSessionServiceImpl(sessionRepository, zignSecAuthenticationEntityRepository, zignSecService, authenticationEventPublisher, objectMapper, metrics)
    }

    @Test
    fun authSuccess() {
        val id = UUID.randomUUID()

        whenever(zignSecService.auth(startAuthRequest)).thenReturn(
            ZignSecResponse(
                id,
                emptyList(),
                "redirect url"
            )
        )

        val response = classUnderTest.auth(startAuthRequest)

        assertThat(response).isInstanceOf(Success::class.java)
        assertThat((response as Success).redirectUrl).isEqualTo("redirect url")

        verify(sessionRepository).save(any())
    }

    @Test
    fun authFailed() {
        whenever(zignSecService.auth(startAuthRequest)).thenReturn(
            ZignSecResponse(
                UUID.randomUUID(),
                listOf(ZignSecResponseError(0, "some_error")),
                "redirect url"
            )
        )

        val response = classUnderTest.auth(startAuthRequest)

        assertThat(response).isInstanceOf(Failed::class.java)
        assertThat((response as Failed).errors).isNotEmpty

        verify(sessionRepository, never()).save(any())
    }

    @Test
    fun signSuccess() {
        val id = UUID.randomUUID()

        whenever(zignSecService.auth(startSignRequest)).thenReturn(
            ZignSecResponse(
                id,
                emptyList(),
                "redirect url"
            )
        )

        val response = classUnderTest.sign(startSignRequest)

        assertThat(response).isInstanceOf(Success::class.java)
        assertThat((response as Success).redirectUrl).isEqualTo("redirect url")

        verify(sessionRepository).save(any())
    }

    @Test
    fun signFailed() {
        whenever(zignSecService.auth(startSignRequest)).thenReturn(
            ZignSecResponse(
                UUID.randomUUID(),
                listOf(ZignSecResponseError(0, "some_error")),
                "redirect url"
            )
        )

        val response = classUnderTest.sign(startSignRequest)

        assertThat(response).isInstanceOf(Failed::class.java)
        assertThat((response as Failed).errors).isNotEmpty

        verify(sessionRepository, never()).save(any())
    }

    @Test
    fun authReuseInitiatedPendingSession() {
        whenever(sessionRepository.findByMemberId(startAuthRequest.memberId.toLong())).thenReturn(
            Optional.of(ZignSecSession(
                memberId = 1337,
                requestType = ZignSecAuthenticationType.AUTH,
                status = ZignSecBankIdProgressStatus.INITIATED,
                referenceId = REFERENCE_ID,
                redirectUrl = "redirect url",
                requestPersonalNumber = null,
                authenticationMethod = ZignSecAuthenticationMethod.NORWAY_WEB_OR_MOBILE
            ))
        )

        whenever(zignSecService.collect(REFERENCE_ID)).thenReturn(
            ZignSecCollectResponse(
                REFERENCE_ID,
                ZignSecCollectResult(
                    ZignSecCollectIdentity(
                        ZignSecCollectState.PENDING
                    )
                )
            )
        )

        val response = classUnderTest.auth(startAuthRequest)

        verify(sessionRepository, never()).save(any())
        assertThat(response).isInstanceOf(Success::class.java)
        assertThat((response as Success).redirectUrl).isEqualTo("redirect url")
    }

    @Test
    fun authDontReuseFinishedSession() {
        val id = UUID.randomUUID()

        whenever(sessionRepository.findByMemberId(startAuthRequest.memberId.toLong())).thenReturn(
            Optional.of(ZignSecSession(
                memberId = 1337,
                requestType = ZignSecAuthenticationType.AUTH,
                status = ZignSecBankIdProgressStatus.INITIATED,
                referenceId = REFERENCE_ID,
                redirectUrl = "redirect url",
                requestPersonalNumber = null,
                authenticationMethod = ZignSecAuthenticationMethod.NORWAY_WEB_OR_MOBILE
            ))
        )

        whenever(zignSecService.collect(REFERENCE_ID)).thenReturn(
            ZignSecCollectResponse(
                REFERENCE_ID,
                ZignSecCollectResult(
                    ZignSecCollectIdentity(
                        ZignSecCollectState.FINISHED
                    )
                )
            )
        )

        whenever(zignSecService.auth(startAuthRequest)).thenReturn(
            ZignSecResponse(
                id,
                emptyList(),
                "redirect url 2"
            )
        )


        val response = classUnderTest.auth(startAuthRequest)

        verify(sessionRepository).save(any())
        assertThat(response).isInstanceOf(Success::class.java)
        assertThat((response as Success).redirectUrl).isEqualTo("redirect url 2")
    }


    @Test
    fun authStartNewSessionIfCollectFails() {
        val id = UUID.randomUUID()

        whenever(sessionRepository.findByMemberId(startAuthRequest.memberId.toLong())).thenReturn(
            Optional.of(ZignSecSession(
                memberId = 1337,
                requestType = ZignSecAuthenticationType.AUTH,
                status = ZignSecBankIdProgressStatus.INITIATED,
                referenceId = REFERENCE_ID,
                redirectUrl = "redirect url",
                requestPersonalNumber = null,
                authenticationMethod = ZignSecAuthenticationMethod.NORWAY_WEB_OR_MOBILE
            ))
        )

        whenever(zignSecService.collect(REFERENCE_ID)).thenThrow(RuntimeException())

        whenever(zignSecService.auth(startAuthRequest)).thenReturn(
            ZignSecResponse(
                id,
                emptyList(),
                "redirect url 2"
            )
        )


        val response = classUnderTest.auth(startAuthRequest)

        verify(sessionRepository).save(any())
        assertThat(response).isInstanceOf(Success::class.java)
        assertThat((response as Success).redirectUrl).isEqualTo("redirect url 2")
    }

    @Test
    fun authDontReuseCompletedSession() {
        val id = UUID.randomUUID()

        whenever(sessionRepository.findByMemberId(startAuthRequest.memberId.toLong())).thenReturn(
            Optional.of(ZignSecSession(
                memberId = 1337,
                requestType = ZignSecAuthenticationType.AUTH,
                status = ZignSecBankIdProgressStatus.COMPLETED,
                referenceId = REFERENCE_ID,
                redirectUrl = "redirect url",
                requestPersonalNumber = null,
                authenticationMethod = ZignSecAuthenticationMethod.NORWAY_WEB_OR_MOBILE
            ))
        )

        whenever(zignSecService.auth(startAuthRequest)).thenReturn(
            ZignSecResponse(
                id,
                emptyList(),
                "redirect url 2"
            )
        )


        val response = classUnderTest.auth(startAuthRequest)

        assertThat(response).isInstanceOf(Success::class.java)
        assertThat((response as Success).redirectUrl).isEqualTo("redirect url 2")
    }

    @Test
    fun handleAuthenticationSuccessNotification() {
        val timestamp = Instant.now()
        val session = ZignSecSession(
            referenceId = REFERENCE_ID,
            memberId = 1337,
            redirectUrl = REDIRECT_URL,
            status = ZignSecBankIdProgressStatus.INITIATED,
            requestType = ZignSecAuthenticationType.AUTH,
            notification = null,
            createdAt = timestamp,
            updatedAt = timestamp,
            requestPersonalNumber = null,
            authenticationMethod = ZignSecAuthenticationMethod.NORWAY_WEB_OR_MOBILE
        )

        whenever(sessionRepository.findByReferenceId(REFERENCE_ID)).thenReturn(
            Optional.of(session)
        )
        whenever(zignSecAuthenticationEntityRepository.findByIdProviderPersonId("9578-6000-4-365161")).thenReturn(
            ZignSecAuthenticationEntity(
                personalNumber = "1212120000",
                idProviderPersonId = "9578-6000-4-365161"
            )
        )

        classUnderTest.handleNotification(zignSecSuccessAuthNotificationRequest)

        verify(authenticationEventPublisher).publishAuthenticationEvent(ZignSecAuthenticationResult.Completed(identity, REFERENCE_ID, 1337, "1212120000", ZignSecAuthenticationMethod.NORWAY_WEB_OR_MOBILE))

        val savedSession = sessionRepository.findByReferenceId(REFERENCE_ID).get()
        assertThat(savedSession.referenceId).isEqualTo(REFERENCE_ID)
        assertThat(savedSession.memberId).isEqualTo(1337)
        assertThat(savedSession.status).isEqualTo(ZignSecBankIdProgressStatus.COMPLETED)
        assertThat(savedSession.requestType).isEqualTo(ZignSecAuthenticationType.AUTH)
        assertThat(savedSession.notification).isNotNull
    }

    @Test
    fun handleAuthenticationSuccessNotificationWithRequestPersonalNumberAndNoAuthenticationEntity() {
        val timestamp = Instant.now()
        val session = ZignSecSession(
            referenceId = REFERENCE_ID,
            memberId = 1337,
            redirectUrl = REDIRECT_URL,
            status = ZignSecBankIdProgressStatus.INITIATED,
            requestType = ZignSecAuthenticationType.AUTH,
            notification = null,
            createdAt = timestamp,
            updatedAt = timestamp,
            requestPersonalNumber = "1212120000",
            authenticationMethod = ZignSecAuthenticationMethod.NORWAY_WEB_OR_MOBILE
        )

        whenever(sessionRepository.findByReferenceId(REFERENCE_ID)).thenReturn(
            Optional.of(session)
        )
        whenever(zignSecAuthenticationEntityRepository.findByIdProviderPersonId("9578-6000-4-365161")).thenReturn(null)
        whenever(zignSecAuthenticationEntityRepository.save<ZignSecAuthenticationEntity>(any())).thenReturn(ZignSecAuthenticationEntity(
            personalNumber = "1212120000",
            idProviderPersonId = "9578-6000-4-365161"
        ))

        classUnderTest.handleNotification(zignSecSuccessAuthNotificationRequest)

        verify(authenticationEventPublisher).publishAuthenticationEvent(ZignSecAuthenticationResult.Completed(identity, REFERENCE_ID, 1337, "1212120000", ZignSecAuthenticationMethod.NORWAY_WEB_OR_MOBILE))

        val savedSession = sessionRepository.findByReferenceId(REFERENCE_ID).get()
        assertThat(savedSession.referenceId).isEqualTo(REFERENCE_ID)
        assertThat(savedSession.memberId).isEqualTo(1337)
        assertThat(savedSession.status).isEqualTo(ZignSecBankIdProgressStatus.COMPLETED)
        assertThat(savedSession.requestType).isEqualTo(ZignSecAuthenticationType.AUTH)
        assertThat(savedSession.notification).isNotNull
    }

    @Test
    fun handleAuthenticationSuccessNotificationWithNoRequestPersonalNumberAndNoAuthenticationEntityFails() {
        val timestamp = Instant.now()
        val session = ZignSecSession(
            referenceId = REFERENCE_ID,
            memberId = 1337,
            redirectUrl = REDIRECT_URL,
            status = ZignSecBankIdProgressStatus.INITIATED,
            requestType = ZignSecAuthenticationType.AUTH,
            notification = null,
            createdAt = timestamp,
            updatedAt = timestamp,
            requestPersonalNumber = null,
            authenticationMethod = ZignSecAuthenticationMethod.NORWAY_WEB_OR_MOBILE
        )

        whenever(sessionRepository.findByReferenceId(REFERENCE_ID)).thenReturn(
            Optional.of(session)
        )
        whenever(zignSecAuthenticationEntityRepository.findByIdProviderPersonId("9578-6000-4-365161")).thenReturn(null)

        classUnderTest.handleNotification(zignSecSuccessAuthNotificationRequest)

        verify(authenticationEventPublisher).publishAuthenticationEvent(ZignSecAuthenticationResult.Failed(REFERENCE_ID, 1337))
    }

    @Test
    fun handleSignSuccessNotification() {
        val timestamp = Instant.now()
        val session = ZignSecSession(
            referenceId = REFERENCE_ID,
            memberId = 1337,
            redirectUrl = REDIRECT_URL,
            status = ZignSecBankIdProgressStatus.INITIATED,
            requestType = ZignSecAuthenticationType.SIGN,
            notification = null,
            createdAt = timestamp,
            updatedAt = timestamp,
            requestPersonalNumber = "12121200000",
            authenticationMethod = ZignSecAuthenticationMethod.NORWAY_WEB_OR_MOBILE
        )

        whenever(sessionRepository.findByReferenceId(REFERENCE_ID)).thenReturn(
            Optional.of(session)
        )

        whenever(zignSecAuthenticationEntityRepository.save(secAuthenticationEntityCaptor.capture())).thenReturn(
            ZignSecAuthenticationEntity(
                personalNumber = "12121200000",
                idProviderPersonId = "9578-6000-4-365161"
            )
        )

        classUnderTest.handleNotification(zignSecSuccessAuthNotificationRequest)

        verify(authenticationEventPublisher).publishSignEvent(ZignSecSignResult.Signed(REFERENCE_ID, 1337, "12121200000", zignSecSuccessAuthNotificationRequest, ZignSecAuthenticationMethod.NORWAY_WEB_OR_MOBILE, "first", "last"))

        assertThat(secAuthenticationEntityCaptor.value.personalNumber).isEqualTo("12121200000")
        assertThat(secAuthenticationEntityCaptor.value.idProviderPersonId).isEqualTo("9578-6000-4-365161")

        val savedSession = sessionRepository.findByReferenceId(REFERENCE_ID).get()
        assertThat(savedSession.referenceId).isEqualTo(REFERENCE_ID)
        assertThat(savedSession.memberId).isEqualTo(1337)
        assertThat(savedSession.status).isEqualTo(ZignSecBankIdProgressStatus.COMPLETED)
        assertThat(savedSession.requestType).isEqualTo(ZignSecAuthenticationType.SIGN)
        assertThat(savedSession.notification).isNotNull
    }

    @Test
    fun handleSignFailedNotification() {
        val timestamp = Instant.now()
        val session = ZignSecSession(
            referenceId = REFERENCE_ID,
            memberId = 1337,
            status = ZignSecBankIdProgressStatus.INITIATED,
            requestType = ZignSecAuthenticationType.SIGN,
            redirectUrl = REDIRECT_URL,
            notification = null,
            createdAt = timestamp,
            updatedAt = timestamp,
            requestPersonalNumber = "",
            authenticationMethod = ZignSecAuthenticationMethod.NORWAY_WEB_OR_MOBILE
        )

        whenever(sessionRepository.findByReferenceId(REFERENCE_ID)).thenReturn(
            Optional.of(session)
        )

        classUnderTest.handleNotification(zignSecFailedAuthNotificationRequest)

        verify(authenticationEventPublisher).publishSignEvent(ZignSecSignResult.Failed(REFERENCE_ID, 1337, ZignSecAuthenticationMethod.NORWAY_WEB_OR_MOBILE))

        val savedSession = sessionRepository.findByReferenceId(REFERENCE_ID).get()
        assertThat(savedSession.referenceId).isEqualTo(REFERENCE_ID)
        assertThat(savedSession.status).isEqualTo(ZignSecBankIdProgressStatus.FAILED)
        assertThat(savedSession.requestType).isEqualTo(ZignSecAuthenticationType.SIGN)
    }

    @Test
    fun handleSecondCompletedNotificationRequests() {
        val timestamp = Instant.now()
        val session = ZignSecSession(
            referenceId = REFERENCE_ID,
            memberId = 1337,
            status = ZignSecBankIdProgressStatus.COMPLETED,
            requestType = ZignSecAuthenticationType.AUTH,
            redirectUrl = REDIRECT_URL,
            notification = null,
            createdAt = timestamp,
            updatedAt = timestamp,
            requestPersonalNumber = null,
            authenticationMethod = ZignSecAuthenticationMethod.NORWAY_WEB_OR_MOBILE
        )

        whenever(sessionRepository.findByReferenceId(REFERENCE_ID)).thenReturn(
            Optional.of(session)
        )

        classUnderTest.handleNotification(zignSecSuccessAuthNotificationRequest)

        verifyZeroInteractions(authenticationEventPublisher)
        verify(sessionRepository, never()).save(any())
    }


    @Test
    fun signDontReuseSessionWithOldPersonalNumber() {
        val id = UUID.randomUUID()

        whenever(sessionRepository.findByMemberId(startSignRequest.memberId.toLong())).thenReturn(
            Optional.of(ZignSecSession(
                memberId = 1337,
                requestType = ZignSecAuthenticationType.SIGN,
                status = ZignSecBankIdProgressStatus.INITIATED,
                referenceId = REFERENCE_ID,
                redirectUrl = "redirect url",
                requestPersonalNumber = "12121212121",
                authenticationMethod = ZignSecAuthenticationMethod.NORWAY_WEB_OR_MOBILE
            ))
        )

        whenever(zignSecService.auth(startSignRequest)).thenReturn(
            ZignSecResponse(
                id,
                emptyList(),
                "redirect url 2"
            )
        )

        whenever(sessionRepository.save(captor.capture()))
            .thenReturn(
                ZignSecSession(
                    memberId = 1337,
                    requestType = ZignSecAuthenticationType.SIGN,
                    status = ZignSecBankIdProgressStatus.INITIATED,
                    referenceId = REFERENCE_ID,
                    redirectUrl = "redirect url",
                    requestPersonalNumber = "12121212120",
                    authenticationMethod = ZignSecAuthenticationMethod.NORWAY_WEB_OR_MOBILE
                )
            )


        val response = classUnderTest.sign(startSignRequest)

        assertThat(captor.value.requestPersonalNumber).isEqualTo("12121212120")
        assertThat((response as Success).redirectUrl).isEqualTo("redirect url 2")
    }

    @Test
    fun failSessionIfMethodChanges() {
        whenever(sessionRepository.findByMemberId(startAuthRequest.memberId.toLong())).thenReturn(
            Optional.of(ZignSecSession(
                memberId = 1337,
                requestType = ZignSecAuthenticationType.SIGN,
                status = ZignSecBankIdProgressStatus.INITIATED,
                referenceId = REFERENCE_ID,
                redirectUrl = "redirect url",
                requestPersonalNumber = "12121212120",
                authenticationMethod = ZignSecAuthenticationMethod.NORWAY_WEB_OR_MOBILE
            ))
        )

        val response = classUnderTest.auth(startSignRequest)

        verify(sessionRepository).delete(any())
        assertThat(response).isInstanceOf(Failed::class.java)
    }

    @Ignore
    @Test
    fun failSessionSigningIfPersonalNumberHasChanged() {
        val timestamp = Instant.now()
        val session = ZignSecSession(
            referenceId = REFERENCE_ID,
            memberId = 1337,
            status = ZignSecBankIdProgressStatus.INITIATED,
            requestType = ZignSecAuthenticationType.SIGN,
            redirectUrl = REDIRECT_URL,
            notification = null,
            createdAt = timestamp,
            updatedAt = timestamp,
            requestPersonalNumber = "01010100000",
            authenticationMethod = ZignSecAuthenticationMethod.NORWAY_WEB_OR_MOBILE
        )

        whenever(sessionRepository.findByReferenceId(REFERENCE_ID)).thenReturn(
            Optional.of(session)
        )

        classUnderTest.handleNotification(zignSecSuccessAuthNotificationRequest)

        verify(authenticationEventPublisher).publishSignEvent(ZignSecSignResult.Failed(REFERENCE_ID, 1337, ZignSecAuthenticationMethod.NORWAY_WEB_OR_MOBILE))
    }

    @Test
    fun validateNorwegianPersonNumberAgainstDateOfBirth() {
        val personNumber1 = "12121212120"
        val birthDate1 = "1912-12-12"
        assertThat(classUnderTest.validatePersonNumberAgainstDateOfBirth(personNumber1, birthDate1, ZignSecAuthenticationMethod.NORWAY_WEB_OR_MOBILE)).isTrue()

        val personNumber2 = "20059412120"
        val birthDate2 = "1994-05-20"
        assertThat(classUnderTest.validatePersonNumberAgainstDateOfBirth(personNumber2, birthDate2, ZignSecAuthenticationMethod.NORWAY_WEB_OR_MOBILE)).isTrue()

        val personNumber3 = "29018912120"
        val birthDate3 = "1989-01-29"
        assertThat(classUnderTest.validatePersonNumberAgainstDateOfBirth(personNumber3, birthDate3, ZignSecAuthenticationMethod.NORWAY_WEB_OR_MOBILE)).isTrue()

        val notMatchingBirthdate = "1989-01-31"
        assertThat(classUnderTest.validatePersonNumberAgainstDateOfBirth(personNumber1, notMatchingBirthdate, ZignSecAuthenticationMethod.NORWAY_WEB_OR_MOBILE)).isFalse()
        assertThat(classUnderTest.validatePersonNumberAgainstDateOfBirth(personNumber2, notMatchingBirthdate, ZignSecAuthenticationMethod.NORWAY_WEB_OR_MOBILE)).isFalse()
        assertThat(classUnderTest.validatePersonNumberAgainstDateOfBirth(personNumber3, notMatchingBirthdate, ZignSecAuthenticationMethod.NORWAY_WEB_OR_MOBILE)).isFalse()
    }

    @Test
    fun validateNorwegianPersonalNumberAgainstNorwegianBankIdBugDateOfBirth() {
        val personNumber1 = "12121212120"
        val norwegianBankIdBugBirthDate1 = "1912-12-11"
        assertThat(classUnderTest.validatePersonNumberAgainstDateOfBirth(personNumber1, norwegianBankIdBugBirthDate1, ZignSecAuthenticationMethod.NORWAY_WEB_OR_MOBILE)).isTrue()

        val personNumber2 = "20059412120"
        val norwegianBankIdBugBirthDate2 = "1994-05-19"
        assertThat(classUnderTest.validatePersonNumberAgainstDateOfBirth(personNumber2, norwegianBankIdBugBirthDate2, ZignSecAuthenticationMethod.NORWAY_WEB_OR_MOBILE)).isTrue()

        val personNumber3 = "02012012120"
        val norwegianBankIdBugBirthDate3 = "2020-01-01"
        assertThat(classUnderTest.validatePersonNumberAgainstDateOfBirth(personNumber3, norwegianBankIdBugBirthDate3, ZignSecAuthenticationMethod.NORWAY_WEB_OR_MOBILE)).isTrue()
    }

    @Test
    fun validateNorwegianDNumberAgainstDateOfBirth() {
        val norwegianDNumber1 = "24059412120"
        val birthDate1 = "1994-05-20"
        assertThat(classUnderTest.validatePersonNumberAgainstDateOfBirth(norwegianDNumber1, birthDate1, ZignSecAuthenticationMethod.NORWAY_WEB_OR_MOBILE)).isTrue()

        val norwegianDNumber2 = "33018912120"
        val birthDate2 = "1989-01-29"
        assertThat(classUnderTest.validatePersonNumberAgainstDateOfBirth(norwegianDNumber2, birthDate2, ZignSecAuthenticationMethod.NORWAY_WEB_OR_MOBILE)).isTrue()

        val norwegianDNumber3 = "05012012120"
        val birthDate3 = "2020-01-01"
        assertThat(classUnderTest.validatePersonNumberAgainstDateOfBirth(norwegianDNumber3, birthDate3, ZignSecAuthenticationMethod.NORWAY_WEB_OR_MOBILE)).isTrue()
    }

    @Test
    fun validateDanishPersonNumberAgainstDateOfBirth() {
        val personNumber1 = "1408300921"
        val birthDate1 = "1930-08-14"
        assertThat(classUnderTest.validatePersonNumberAgainstDateOfBirth(personNumber1, birthDate1, ZignSecAuthenticationMethod.DENMARK)).isTrue()

        val personNumber2 = "1507161027"
        val birthDate2 = "2016-07-15"
        assertThat(classUnderTest.validatePersonNumberAgainstDateOfBirth(personNumber2, birthDate2, ZignSecAuthenticationMethod.DENMARK)).isTrue()
    }

    @Test
    fun validateNorwegianDNumberAndNorwegianBugDoseNotWorkInDenmark() {
        val norwegianDNumber = "24059412120"
        val birthDateForDNumber = "1994-05-20"
        assertThat(classUnderTest.validatePersonNumberAgainstDateOfBirth(norwegianDNumber, birthDateForDNumber, ZignSecAuthenticationMethod.DENMARK)).isFalse()

        val personNumber = "30018912120"
        val norwegianBankIdBugBirthDate = "1989-01-29"
        assertThat(classUnderTest.validatePersonNumberAgainstDateOfBirth(personNumber, norwegianBankIdBugBirthDate, ZignSecAuthenticationMethod.DENMARK)).isFalse()
    }

    companion object {
        val startSignRequest = ZignSecBankIdAuthenticationRequest(
            "1337",
            "12121212120",
            "NO",
            "success",
            "fail",
            ZignSecAuthenticationMethod.NORWAY_WEB_OR_MOBILE
        )

        val startAuthRequest = ZignSecBankIdAuthenticationRequest(
            "1337",
            null,
            "NO",
            "success",
            "fail",
            ZignSecAuthenticationMethod.NORWAY_WEB_OR_MOBILE
        )

        val REDIRECT_URL = "redirect_url"

        val REFERENCE_ID: UUID = UUID.fromString("a42a8afe-4071-4e99-8f9f-757c5942e1e5")!!

        val identity: Identity = Identity(
            countryCode = "NO",
            firstName = "first",
            lastName = "last",
            fullName = "first last",
            personalNumber = null,
            dateOfBirth = "2012-12-12",
            age = 8,
            gender = "",
            idProviderName = "BankIDNO",
            identificationDate = LocalDateTime.parse("2020-02-11T15:45:23"),
            idProviderRequestId = "",
            idProviderPersonId = "9578-6000-4-365161",
            customerPersonId = ""
        )

        val zignSecSuccessAuthNotificationRequest = """
            {
              "id": "a42a8afe-4071-4e99-8f9f-757c5942e1e5",
              "errors": [],
              "identity": {
                "CountryCode": "NO",
                "FirstName": "first",
                "LastName": "last",
                "FullName": "first last",
                "DateOfBirth": "2012-12-12",
                "Age": 8,
                "Gender": "",
                "IdProviderName": "BankIDNO",
                "IdentificationDate": "2020-02-11T15:45:23Z",
                "IdProviderRequestId": "",
                "IdProviderPersonId": "9578-6000-4-365161",
                "CustomerPersonId": ""
              },
              "BANKIdNO_OIDC": "{\r\n  \"access_token\": \"access_token\",\r\n  \"expires_in\": 300,\r\n  \"refresh_expires_in\": 1800,\r\n  \"refresh_token\": \"access_token\",\r\n  \"token_type\": \"bearer\",\r\n  \"id_token\": \"id_token\",\r\n  \"not-before-policy\": 0,\r\n  \"session_state\": \"session_state\",\r\n  \"scope\": \"openid nnin_altsub profile\"\r\n}",
              "method": "nbid_oidc"
            }
        """.trimIndent()

        val zignSecFailedAuthNotificationRequest =
            """
            {
              "id": "a42a8afe-4071-4e99-8f9f-757c5942e1e5",
              "errors": [
                    {
                        "code": 0,
                        "description": "some error"
                    }
                ],
                "method": "nbid_oidc"
            }
        """.trimIndent()
    }
}
