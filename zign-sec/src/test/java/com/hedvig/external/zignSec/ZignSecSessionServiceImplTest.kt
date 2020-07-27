package com.hedvig.external.zignSec

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.hedvig.external.authentication.dto.NorwegianAuthenticationResult
import com.hedvig.external.authentication.dto.NorwegianSignResult
import com.hedvig.external.authentication.dto.NorwegianBankIdAuthenticationRequest
import com.hedvig.external.authentication.dto.NorwegianBankIdProgressStatus
import com.hedvig.external.authentication.dto.StartNorwegianAuthenticationResult.Success
import com.hedvig.external.authentication.dto.StartNorwegianAuthenticationResult.Failed
import com.hedvig.external.event.NorwegianAuthenticationEventPublisher
import com.hedvig.external.zignSec.client.dto.ZignSecCollectIdentity
import com.hedvig.external.zignSec.client.dto.ZignSecCollectResponse
import com.hedvig.external.zignSec.client.dto.ZignSecCollectResult
import com.hedvig.external.zignSec.client.dto.ZignSecCollectState
import com.hedvig.external.zignSec.client.dto.ZignSecResponse
import com.hedvig.external.zignSec.client.dto.ZignSecResponseError
import com.hedvig.external.zignSec.repository.ZignSecSessionRepository
import com.hedvig.external.zignSec.repository.ZignSecSignEntityRepository
import com.hedvig.external.zignSec.repository.entitys.NorwegianAuthenticationType
import com.hedvig.external.zignSec.repository.entitys.ZignSecSession
import com.hedvig.external.zignSec.repository.entitys.ZignSecSignEntity
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
import java.util.*

@RunWith(MockitoJUnitRunner::class)
class ZignSecSessionServiceImplTest {

    @Mock
    lateinit var sessionRepository: ZignSecSessionRepository

    @Mock
    lateinit var zignSecSignEntityRepository: ZignSecSignEntityRepository

    @Mock
    lateinit var zignSecService: ZignSecService

    @Mock
    lateinit var norwegianAuthenticationEventPublisher: NorwegianAuthenticationEventPublisher

    @Captor
    lateinit var captor: ArgumentCaptor<ZignSecSession>

    @Captor
    lateinit var secSignEntityCaptor: ArgumentCaptor<ZignSecSignEntity>

    lateinit var objectMapper: ObjectMapper

    private lateinit var classUnderTest: ZignSecSessionServiceImpl

    @Before
    fun before() {
        objectMapper = ObjectMapper().registerKotlinModule().registerModule(JavaTimeModule())
        classUnderTest = ZignSecSessionServiceImpl(sessionRepository, zignSecSignEntityRepository, zignSecService, norwegianAuthenticationEventPublisher, objectMapper)
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
                requestType = NorwegianAuthenticationType.AUTH,
                status = NorwegianBankIdProgressStatus.INITIATED,
                referenceId = REFERENCE_ID,
                redirectUrl = "redirect url",
                requestPersonalNumber = null
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
                requestType = NorwegianAuthenticationType.AUTH,
                status = NorwegianBankIdProgressStatus.INITIATED,
                referenceId = REFERENCE_ID,
                redirectUrl = "redirect url",
                requestPersonalNumber = null
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
                requestType = NorwegianAuthenticationType.AUTH,
                status = NorwegianBankIdProgressStatus.INITIATED,
                referenceId = REFERENCE_ID,
                redirectUrl = "redirect url",
                requestPersonalNumber = null
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
                requestType = NorwegianAuthenticationType.AUTH,
                status = NorwegianBankIdProgressStatus.COMPLETED,
                referenceId = REFERENCE_ID,
                redirectUrl = "redirect url",
                requestPersonalNumber = null
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
            status = NorwegianBankIdProgressStatus.INITIATED,
            requestType = NorwegianAuthenticationType.AUTH,
            notification = null,
            createdAt = timestamp,
            updatedAt = timestamp,
            requestPersonalNumber = null
        )

        whenever(sessionRepository.findByReferenceId(REFERENCE_ID)).thenReturn(
            Optional.of(session)
        )
        whenever(zignSecSignEntityRepository.findByIdProviderPersonId("9578-6000-4-365161")).thenReturn(
            ZignSecSignEntity(
                personalNumber = "1212120000",
                idProviderPersonId = "9578-6000-4-365161"
            )
        )

        classUnderTest.handleNotification(zignSecSuccessAuthNotificationRequest)

        verify(norwegianAuthenticationEventPublisher).publishAuthenticationEvent(NorwegianAuthenticationResult.Completed(REFERENCE_ID, 1337, "1212120000"))

        val savedSession = sessionRepository.findByReferenceId(REFERENCE_ID).get()
        assertThat(savedSession.referenceId).isEqualTo(REFERENCE_ID)
        assertThat(savedSession.memberId).isEqualTo(1337)
        assertThat(savedSession.status).isEqualTo(NorwegianBankIdProgressStatus.COMPLETED)
        assertThat(savedSession.requestType).isEqualTo(NorwegianAuthenticationType.AUTH)
        assertThat(savedSession.notification).isNotNull
    }

    @Test
    fun handleSignSuccessNotification() {
        val timestamp = Instant.now()
        val session = ZignSecSession(
            referenceId = REFERENCE_ID,
            memberId = 1337,
            redirectUrl = REDIRECT_URL,
            status = NorwegianBankIdProgressStatus.INITIATED,
            requestType = NorwegianAuthenticationType.SIGN,
            notification = null,
            createdAt = timestamp,
            updatedAt = timestamp,
            requestPersonalNumber = "12121200000"
        )

        whenever(sessionRepository.findByReferenceId(REFERENCE_ID)).thenReturn(
            Optional.of(session)
        )

        whenever(zignSecSignEntityRepository.save(secSignEntityCaptor.capture())).thenReturn(
            ZignSecSignEntity(
                personalNumber = "12121200000",
                idProviderPersonId = "9578-6000-4-365161"
            )
        )

        classUnderTest.handleNotification(zignSecSuccessAuthNotificationRequest)

        verify(norwegianAuthenticationEventPublisher).publishSignEvent(NorwegianSignResult.Signed(REFERENCE_ID, 1337, "12121200000", zignSecSuccessAuthNotificationRequest))

        assertThat(secSignEntityCaptor.value.personalNumber).isEqualTo("12121200000")
        assertThat(secSignEntityCaptor.value.idProviderPersonId).isEqualTo("9578-6000-4-365161")

        val savedSession = sessionRepository.findByReferenceId(REFERENCE_ID).get()
        assertThat(savedSession.referenceId).isEqualTo(REFERENCE_ID)
        assertThat(savedSession.memberId).isEqualTo(1337)
        assertThat(savedSession.status).isEqualTo(NorwegianBankIdProgressStatus.COMPLETED)
        assertThat(savedSession.requestType).isEqualTo(NorwegianAuthenticationType.SIGN)
        assertThat(savedSession.notification).isNotNull
    }

    @Test
    fun handleSignFailedNotification() {
        val timestamp = Instant.now()
        val session = ZignSecSession(
            referenceId = REFERENCE_ID,
            memberId = 1337,
            status = NorwegianBankIdProgressStatus.INITIATED,
            requestType = NorwegianAuthenticationType.SIGN,
            redirectUrl = REDIRECT_URL,
            notification = null,
            createdAt = timestamp,
            updatedAt = timestamp,
            requestPersonalNumber = ""
        )

        whenever(sessionRepository.findByReferenceId(REFERENCE_ID)).thenReturn(
            Optional.of(session)
        )

        classUnderTest.handleNotification(zignSecFailedAuthNotificationRequest)

        verify(norwegianAuthenticationEventPublisher).publishSignEvent(NorwegianSignResult.Failed(REFERENCE_ID, 1337))

        val savedSession = sessionRepository.findByReferenceId(REFERENCE_ID).get()
        assertThat(savedSession.referenceId).isEqualTo(REFERENCE_ID)
        assertThat(savedSession.status).isEqualTo(NorwegianBankIdProgressStatus.FAILED)
        assertThat(savedSession.requestType).isEqualTo(NorwegianAuthenticationType.SIGN)
    }

    @Test
    fun handleSecondCompletedNotificationRequests() {
        val timestamp = Instant.now()
        val session = ZignSecSession(
            referenceId = REFERENCE_ID,
            memberId = 1337,
            status = NorwegianBankIdProgressStatus.COMPLETED,
            requestType = NorwegianAuthenticationType.AUTH,
            redirectUrl = REDIRECT_URL,
            notification = null,
            createdAt = timestamp,
            updatedAt = timestamp,
            requestPersonalNumber = null
        )

        whenever(sessionRepository.findByReferenceId(REFERENCE_ID)).thenReturn(
            Optional.of(session)
        )

        classUnderTest.handleNotification(zignSecSuccessAuthNotificationRequest)

        verifyZeroInteractions(norwegianAuthenticationEventPublisher)
        verify(sessionRepository, never()).save(any())
    }


    @Test
    fun signDontReuseSessionWithOldPersonalNumber() {
        val id = UUID.randomUUID()

        whenever(sessionRepository.findByMemberId(startSignRequest.memberId.toLong())).thenReturn(
            Optional.of(ZignSecSession(
                memberId = 1337,
                requestType = NorwegianAuthenticationType.SIGN,
                status = NorwegianBankIdProgressStatus.INITIATED,
                referenceId = REFERENCE_ID,
                redirectUrl = "redirect url",
                requestPersonalNumber = "12121212121"
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
                    requestType = NorwegianAuthenticationType.SIGN,
                    status = NorwegianBankIdProgressStatus.INITIATED,
                    referenceId = REFERENCE_ID,
                    redirectUrl = "redirect url",
                    requestPersonalNumber = "12121212120"
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
                requestType = NorwegianAuthenticationType.SIGN,
                status = NorwegianBankIdProgressStatus.INITIATED,
                referenceId = REFERENCE_ID,
                redirectUrl = "redirect url",
                requestPersonalNumber = "12121212120"
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
            status = NorwegianBankIdProgressStatus.INITIATED,
            requestType = NorwegianAuthenticationType.SIGN,
            redirectUrl = REDIRECT_URL,
            notification = null,
            createdAt = timestamp,
            updatedAt = timestamp,
            requestPersonalNumber = "01010100000"
        )

        whenever(sessionRepository.findByReferenceId(REFERENCE_ID)).thenReturn(
            Optional.of(session)
        )

        classUnderTest.handleNotification(zignSecSuccessAuthNotificationRequest)

        verify(norwegianAuthenticationEventPublisher).publishSignEvent(NorwegianSignResult.Failed(REFERENCE_ID, 1337))
    }

    @Test
    fun testSsnAndBirthDateExtensionsWorks() {
        assertThat("12121212120".dayMonthAndTwoDigitYearFromNorwegianSsn()).isEqualTo("1912-12-12".dayMonthAndTwoDigitYearFromDateOfBirth())
        assertThat("20059412120".dayMonthAndTwoDigitYearFromNorwegianSsn()).isEqualTo("1994-05-20".dayMonthAndTwoDigitYearFromDateOfBirth())
        assertThat("29018912120".dayMonthAndTwoDigitYearFromNorwegianSsn()).isEqualTo("1989-01-29".dayMonthAndTwoDigitYearFromDateOfBirth())
    }

    companion object {
        val startSignRequest = NorwegianBankIdAuthenticationRequest(
            "1337",
            "12121212120",
            "NO",
            "success",
            "fail"
        )

        val startAuthRequest = NorwegianBankIdAuthenticationRequest(
            "1337",
            null,
            "NO",
            "success",
            "fail"
        )

        val REDIRECT_URL = "redirect_url"

        val REFERENCE_ID: UUID = UUID.fromString("a42a8afe-4071-4e99-8f9f-757c5942e1e5")!!

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
                "IdentificationDate": "2020-02-11T15:45:23.8368433Z",
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
