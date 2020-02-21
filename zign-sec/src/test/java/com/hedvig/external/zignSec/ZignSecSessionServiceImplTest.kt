package com.hedvig.external.zignSec

import com.hedvig.external.authentication.dto.NorwegianAuthenticationResult
import com.hedvig.external.authentication.dto.NorwegianSignResult
import com.hedvig.external.authentication.dto.NorwegianBankIdAuthenticationRequest
import com.hedvig.external.authentication.dto.NorwegianBankIdProgressStatus
import com.hedvig.external.authentication.dto.StartNorwegianAuthenticationResult.Success
import com.hedvig.external.authentication.dto.StartNorwegianAuthenticationResult.Failed
import com.hedvig.external.event.NorwegianAuthenticationEventPublisher
import com.hedvig.external.zignSec.client.dto.ZignSecIdentity
import com.hedvig.external.zignSec.client.dto.ZignSecNotificationRequest
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
import org.mockito.ArgumentMatchers.same
import org.mockito.Mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyZeroInteractions
import org.mockito.Mockito.`when` as whenever
import org.mockito.junit.MockitoJUnitRunner
import java.time.Instant
import java.time.LocalDate
import java.util.*

@RunWith(MockitoJUnitRunner::class)
class ZignSecSessionServiceImplTest {

    @Mock
    lateinit var sessionRepository: ZignSecSessionRepository

    @Mock
    lateinit var zignSecService: ZignSecService

    @Mock
    lateinit var norwegianAuthenticationEventPublisher: NorwegianAuthenticationEventPublisher

    private lateinit var classUnderTest: ZignSecSessionServiceImpl

    @Before
    fun before() {
        classUnderTest = ZignSecSessionServiceImpl(sessionRepository, zignSecService, norwegianAuthenticationEventPublisher)
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
            sessionId = zignSecSuccessAuthNotificationRequest.id,
            memberId = 1337,
            status = NorwegianBankIdProgressStatus.INITIATED,
            requestType = NorwegianAuthenticationType.AUTH,
            notification = null,
            createdAt = timestamp,
            updatedAt = timestamp
        )

        whenever(sessionRepository.findById(zignSecSuccessAuthNotificationRequest.id)).thenReturn(
            Optional.of(session)
        )

        classUnderTest.handleNotification(zignSecSuccessAuthNotificationRequest)

        verify(norwegianAuthenticationEventPublisher).publishAuthenticationEvent(NorwegianAuthenticationResult.Completed(zignSecSuccessAuthNotificationRequest.id, 1337, "12121212120"))

        val savedSession = sessionRepository.findById(zignSecSuccessAuthNotificationRequest.id).get()
        assertThat(savedSession.sessionId).isEqualTo(zignSecSuccessAuthNotificationRequest.id)
        assertThat(savedSession.memberId).isEqualTo(1337)
        assertThat(savedSession.status).isEqualTo(NorwegianBankIdProgressStatus.COMPLETED)
        assertThat(savedSession.requestType).isEqualTo(NorwegianAuthenticationType.AUTH)
        assertThat(savedSession.notification).isNotNull
    }

    @Test
    fun handleSignFailedNotification() {
        val timestamp = Instant.now()
        val session = ZignSecSession(
            sessionId = zignSecFailedAuthNotificationRequest.id,
            memberId = 1337,
            status = NorwegianBankIdProgressStatus.INITIATED,
            requestType = NorwegianAuthenticationType.SIGN,
            notification = null,
            createdAt = timestamp,
            updatedAt = timestamp
        )

        whenever(sessionRepository.findById(zignSecFailedAuthNotificationRequest.id)).thenReturn(
            Optional.of(session)
        )

        classUnderTest.handleNotification(zignSecFailedAuthNotificationRequest)

        verify(norwegianAuthenticationEventPublisher).publishSignEvent(NorwegianSignResult.Failed(zignSecFailedAuthNotificationRequest.id, 1337))

        val savedSession = sessionRepository.findById(zignSecFailedAuthNotificationRequest.id).get()
        assertThat(savedSession.sessionId).isEqualTo(zignSecFailedAuthNotificationRequest.id)
        assertThat(savedSession.status).isEqualTo(NorwegianBankIdProgressStatus.FAILED)
        assertThat(savedSession.requestType).isEqualTo(NorwegianAuthenticationType.SIGN)
    }

    @Test
    fun handleSecondCompletedNotificationRequests() {
        val timestamp = Instant.now()
        val session = ZignSecSession(
            sessionId = zignSecFailedAuthNotificationRequest.id,
            memberId = 1337,
            status = NorwegianBankIdProgressStatus.COMPLETED,
            requestType = NorwegianAuthenticationType.AUTH,
            notification = null,
            createdAt = timestamp,
            updatedAt = timestamp
        )

        whenever(sessionRepository.findById(zignSecFailedAuthNotificationRequest.id)).thenReturn(
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

        val zignSecSuccessAuthNotificationRequest = ZignSecNotificationRequest(
            UUID.randomUUID(),
            emptyList(),
            ZignSecIdentity(
                "NO",
                "first",
                "last",
                "first mid last",
                "12121212120",
                "12-12-12",
                108,
                "female",
                "first",
                LocalDate.now(),
                "",
                "",
                ""
            ),
            "nbid",
            ""
        )

        val zignSecFailedAuthNotificationRequest = ZignSecNotificationRequest(
            UUID.randomUUID(),
            listOf(ZignSecResponseError(0, "some error")),
            null,
            "nbid",
            ""
        )
    }
}
