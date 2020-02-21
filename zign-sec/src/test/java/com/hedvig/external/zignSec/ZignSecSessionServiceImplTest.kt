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
import com.hedvig.external.zignSec.client.dto.ZignSecResponse
import com.hedvig.external.zignSec.client.dto.ZignSecResponseError
import com.hedvig.external.zignSec.repository.ZignSecSessionRepository
import com.hedvig.external.zignSec.repository.entitys.NorwegianAuthenticationType
import com.hedvig.external.zignSec.repository.entitys.ZignSecSession
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.any
import org.mockito.Mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyZeroInteractions
import org.mockito.Mockito.`when` as whenever
import org.mockito.junit.MockitoJUnitRunner
import java.time.Instant
import java.util.*

@RunWith(MockitoJUnitRunner::class)
class ZignSecSessionServiceImplTest {

    @Mock
    lateinit var sessionRepository: ZignSecSessionRepository

    @Mock
    lateinit var zignSecService: ZignSecService

    @Mock
    lateinit var norwegianAuthenticationEventPublisher: NorwegianAuthenticationEventPublisher

    lateinit var objectMapper: ObjectMapper

    private lateinit var classUnderTest: ZignSecSessionServiceImpl

    @Before
    fun before() {
        objectMapper = ObjectMapper().registerKotlinModule().registerModule(JavaTimeModule())
        classUnderTest = ZignSecSessionServiceImpl(sessionRepository, zignSecService, norwegianAuthenticationEventPublisher, objectMapper)
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
        assertThat((response as Success).id).isEqualTo(id)
        assertThat(response.redirectUrl).isEqualTo("redirect url")

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

        whenever(zignSecService.auth(startAuthRequest)).thenReturn(
            ZignSecResponse(
                id,
                emptyList(),
                "redirect url"
            )
        )

        val response = classUnderTest.auth(startAuthRequest)

        assertThat(response).isInstanceOf(Success::class.java)
        assertThat((response as Success).id).isEqualTo(id)
        assertThat(response.redirectUrl).isEqualTo("redirect url")

        verify(sessionRepository).save(any())
    }

    @Test
    fun signFailed() {
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
    fun handleAuthenticationSuccessNotification() {
        val timestamp = Instant.now()
        val session = ZignSecSession(
            sessionId = SESSION_ID,
            memberId = 1337,
            status = NorwegianBankIdProgressStatus.INITIATED,
            requestType = NorwegianAuthenticationType.AUTH,
            notification = null,
            createdAt = timestamp,
            updatedAt = timestamp
        )

        whenever(sessionRepository.findById(SESSION_ID)).thenReturn(
            Optional.of(session)
        )

        classUnderTest.handleNotification(zignSecSuccessAuthNotificationRequest)

        verify(norwegianAuthenticationEventPublisher).publishAuthenticationEvent(NorwegianAuthenticationResult.Completed(SESSION_ID, 1337, "12121212120"))

        val savedSession = sessionRepository.findById(SESSION_ID).get()
        assertThat(savedSession.sessionId).isEqualTo(SESSION_ID)
        assertThat(savedSession.memberId).isEqualTo(1337)
        assertThat(savedSession.status).isEqualTo(NorwegianBankIdProgressStatus.COMPLETED)
        assertThat(savedSession.requestType).isEqualTo(NorwegianAuthenticationType.AUTH)
        assertThat(savedSession.notification).isNotNull
    }

    @Test
    fun handleSignFailedNotification() {
        val timestamp = Instant.now()
        val session = ZignSecSession(
            sessionId = SESSION_ID,
            memberId = 1337,
            status = NorwegianBankIdProgressStatus.INITIATED,
            requestType = NorwegianAuthenticationType.SIGN,
            notification = null,
            createdAt = timestamp,
            updatedAt = timestamp
        )

        whenever(sessionRepository.findById(SESSION_ID)).thenReturn(
            Optional.of(session)
        )

        classUnderTest.handleNotification(zignSecFailedAuthNotificationRequest)

        verify(norwegianAuthenticationEventPublisher).publishSignEvent(NorwegianSignResult.Failed(SESSION_ID, 1337))

        val savedSession = sessionRepository.findById(SESSION_ID).get()
        assertThat(savedSession.sessionId).isEqualTo(SESSION_ID)
        assertThat(savedSession.status).isEqualTo(NorwegianBankIdProgressStatus.FAILED)
        assertThat(savedSession.requestType).isEqualTo(NorwegianAuthenticationType.SIGN)
    }

    @Test
    fun handleSecondCompletedNotificationRequests() {
        val timestamp = Instant.now()
        val session = ZignSecSession(
            sessionId = SESSION_ID,
            memberId = 1337,
            status = NorwegianBankIdProgressStatus.COMPLETED,
            requestType = NorwegianAuthenticationType.AUTH,
            notification = null,
            createdAt = timestamp,
            updatedAt = timestamp
        )

        whenever(sessionRepository.findById(SESSION_ID)).thenReturn(
            Optional.of(session)
        )

        classUnderTest.handleNotification(zignSecFailedAuthNotificationRequest)

        verifyZeroInteractions(norwegianAuthenticationEventPublisher)
        verify(sessionRepository, never()).save(any())
    }

    companion object {
        val startAuthRequest = NorwegianBankIdAuthenticationRequest(
            "1337",
            "12121212120",
            "NO",
            false
        )

        val SESSION_ID: UUID = UUID.fromString("a42a8afe-4071-4e99-8f9f-757c5942e1e5")!!

        val zignSecSuccessAuthNotificationRequest = """
            {
              "id": "a42a8afe-4071-4e99-8f9f-757c5942e1e5",
              "errors": [],
              "identity": {
                "CountryCode": "NO",
                "FirstName": "first",
                "LastName": "last",
                "FullName": "first last",
                "PersonalNumber": "12121212120",
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
