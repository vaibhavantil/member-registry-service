package com.hedvig.memberservice.services

import com.hedvig.external.bankID.bankIdTypes.BankIdError
import com.hedvig.external.bankID.bankIdTypes.Collect.Cert
import com.hedvig.external.bankID.bankIdTypes.Collect.Device
import com.hedvig.external.bankID.bankIdTypes.Collect.User
import com.hedvig.external.bankID.bankIdTypes.CollectResponse
import com.hedvig.external.bankID.bankIdTypes.CollectStatus
import com.hedvig.external.bankID.bankIdTypes.CompletionData
import com.hedvig.external.bankID.bankIdTypes.OrderResponse
import com.hedvig.memberservice.entities.SignSession
import com.hedvig.memberservice.entities.SignSessionRepository
import com.hedvig.memberservice.entities.SignStatus
import com.hedvig.memberservice.jobs.SwedishBankIdMetrics
import com.hedvig.memberservice.services.member.MemberService
import com.hedvig.memberservice.web.v2.dto.WebsignRequest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExpectedException
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Captor
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.junit.MockitoJUnitRunner
import org.quartz.JobDetail
import org.quartz.Scheduler
import org.quartz.SchedulerException
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset
import org.mockito.Mockito.`when` as whenever
import java.util.*

@RunWith(MockitoJUnitRunner::class)
class SwedishBankIdSigningServiceTest {


    @Mock
    lateinit var bankIdRestService: BankIdRestService

    @Mock
    lateinit var signSessionRepository: SignSessionRepository

    @Mock
    lateinit var scheduler: Scheduler

    @Mock
    lateinit var memberService: MemberService

    @Mock
    lateinit var swedishBankIdMetrics: SwedishBankIdMetrics

    @Rule
    @JvmField
    var thrown = ExpectedException.none()

    @Captor
    lateinit var jobDetailArgumentCaptor: ArgumentCaptor<JobDetail>

    @Captor
    lateinit var argumentCaptor: ArgumentCaptor<String>

    private lateinit var sut: SwedishBankIdSigningService

    @Before
    fun setup() {
        sut = SwedishBankIdSigningService(
            bankIdRestService, signSessionRepository,
            scheduler, memberService, SWITCHER_MESSAGE, NON_SWITCHER_MESSAGE, swedishBankIdMetrics
        )
    }

    @Test
    fun collectBankId_givenSessionInProgress_thenReturnTrue() {
        val session = makeSignSession(SignStatus.IN_PROGRESS)

        whenever(signSessionRepository.findByOrderReference(ORDER_REFERENCE))
            .thenReturn(Optional.of(session))

        val response = makeCollectResponse(CollectStatus.pending)

        whenever(bankIdRestService.collect(ORDER_REFERENCE)).thenReturn(response)

        val actual = sut.collectBankId(ORDER_REFERENCE)

        assertThat(actual).isEqualTo(true)
    }

    @Test
    fun collectBankId_givenSessionThatIsComplete_thenReturnFalse() {

        val session = makeSignSession(SignStatus.COMPLETED)

        whenever(signSessionRepository.findByOrderReference(ORDER_REFERENCE))
            .thenReturn(Optional.of(session))

        val status = sut.collectBankId(ORDER_REFERENCE)

        assertThat(status).isEqualTo(false)

        Mockito.verifyZeroInteractions(bankIdRestService)
    }

    @Test
    fun collectBankId_givenSessionThatIsFailed_thenReturnFalse() {

        val session = makeSignSession(SignStatus.FAILED)

        whenever(signSessionRepository.findByOrderReference(ORDER_REFERENCE))
            .thenReturn(Optional.of(session))

        val status = sut.collectBankId(ORDER_REFERENCE)

        assertThat(status).isEqualTo(false)

        Mockito.verifyZeroInteractions(bankIdRestService)
    }

    @Test
    fun collectBankId_givenNoMatchingOrderReference_thenReturnFalse() {

        whenever(signSessionRepository.findByOrderReference(ORDER_REFERENCE)).thenReturn(Optional.empty())

        val status = sut.collectBankId(ORDER_REFERENCE)

        assertThat(status).isEqualTo(false)

        Mockito.verifyZeroInteractions(bankIdRestService)
    }

    @Test
    fun collectBankId_givenBankIdReturnPending_thenReturnTrue() {

        val session = makeSignSession(SignStatus.IN_PROGRESS)

        whenever(signSessionRepository.findByOrderReference(ORDER_REFERENCE))
            .thenReturn(Optional.of(session))

        val response = makeCollectResponse(CollectStatus.pending)

        whenever(bankIdRestService.collect(ArgumentMatchers.eq(ORDER_REFERENCE))).thenReturn(response)

        val status = sut.collectBankId(ORDER_REFERENCE)

        assertThat(status).isEqualTo(true)

        assertThat(session.collectResponse)
            .hasFieldOrPropertyWithValue("status", CollectStatus.pending)

        Mockito.verify(signSessionRepository).save(ArgumentMatchers.eq(session))
    }

    @Test
    fun collectBankId_givenBankIdReturnComplete_thenReturnFalse() {

        val session = makeSignSession(SignStatus.IN_PROGRESS)

        whenever(signSessionRepository.findByOrderReference(ORDER_REFERENCE))
            .thenReturn(Optional.of(session))

        val response = makeCollectResponse(CollectStatus.complete)

        whenever(bankIdRestService.collect(ArgumentMatchers.eq(ORDER_REFERENCE))).thenReturn(response)

        val status = sut.collectBankId(ORDER_REFERENCE)

        assertThat(status).isEqualTo(false)

        assertThat(session.collectResponse)
            .hasFieldOrPropertyWithValue("status", CollectStatus.complete)

        Mockito.verify(signSessionRepository).save(ArgumentMatchers.eq(session))
    }

    @Test
    fun collectBankId_givenBankIdReturnComplete_thenSendBankIdCompleteToMemberService() {

        val session = makeSignSession(SignStatus.IN_PROGRESS)

        whenever(signSessionRepository.findByOrderReference(ORDER_REFERENCE))
            .thenReturn(Optional.of(session))

        val response = makeCollectResponse(CollectStatus.complete)

        whenever(bankIdRestService.collect(ArgumentMatchers.eq(ORDER_REFERENCE))).thenReturn(response)

        sut.collectBankId(ORDER_REFERENCE)

        Mockito.verify(memberService).bankIdSignComplete(ArgumentMatchers.eq(MEMBER_ID), ArgumentMatchers.eq(response))
    }

    @Test
    fun collectBankId_givenBankIdReturnComplete_thenSetsSignStatusToComplete() {

        val session = makeSignSession(SignStatus.IN_PROGRESS)

        whenever(signSessionRepository.findByOrderReference(ORDER_REFERENCE))
            .thenReturn(Optional.of(session))

        val response = makeCollectResponse(CollectStatus.complete)

        whenever(bankIdRestService.collect(ArgumentMatchers.eq(ORDER_REFERENCE))).thenReturn(response)

        sut.collectBankId(ORDER_REFERENCE)

        assertThat(session.status).isEqualTo(SignStatus.COMPLETED)
    }


    @Test
    fun collectBankId_givenBankIdReturnFailed_thenSetsSignStatusToFailed() {
        val session = makeSignSession(SignStatus.IN_PROGRESS)

        whenever(signSessionRepository.findByOrderReference(ORDER_REFERENCE))
            .thenReturn(Optional.of(session))

        val response = makeCollectResponse(CollectStatus.failed)

        whenever(bankIdRestService.collect(ArgumentMatchers.eq(ORDER_REFERENCE))).thenReturn(response)

        sut.collectBankId(ORDER_REFERENCE)

        assertThat(session.status).isEqualTo(SignStatus.FAILED)
    }

    @Test
    @Throws(SchedulerException::class)
    fun startSign_shouldSchedulesCollectJob() {
        whenever(bankIdRestService.startSign(ArgumentMatchers.matches(SSN), ArgumentMatchers.anyString(), ArgumentMatchers.anyString()))
            .thenReturn(makeOrderResponse())

        whenever(scheduler.scheduleJob(jobDetailArgumentCaptor.capture(), ArgumentMatchers.any())).thenReturn(Date.from(
            Instant.now()))

        sut.startSign(WebsignRequest(EMAIL, SSN, IP_ADDRESS), MEMBER_ID, false)

        assertThat(jobDetailArgumentCaptor.value.key.name).isEqualTo(ORDER_REFERENCE)
    }

    @Test
    fun startWebSign_givenNonReusableBankIdSession_thenCallBankIdReturnOrderRefAndAutoStartToken() {

        val signSession = Mockito.mock(SignSession::class.java)

        whenever(signSession.canReuseBankIdSession()).thenReturn(false)

        whenever(signSessionRepository.findByMemberId(MEMBER_ID)).thenReturn(Optional.of(signSession))

        whenever(bankIdRestService.startSign(ArgumentMatchers.matches(SSN), ArgumentMatchers.anyString(), ArgumentMatchers.anyString()))
            .thenReturn(makeOrderResponse(ORDER_REFERENCE2, AUTO_START_TOKEN2))

        val response = sut.startSign(WebsignRequest(EMAIL, SSN, IP_ADDRESS), MEMBER_ID, false)

        assertThat(response.bankIdOrderResponse).hasFieldOrPropertyWithValue("orderRef", ORDER_REFERENCE2)

        assertThat(response.bankIdOrderResponse).hasFieldOrPropertyWithValue("autoStartToken",
            AUTO_START_TOKEN2)
    }


    @Test
    fun startSign_givenNonSwitchingMember_thenSendNonSwitchingMessage() {
        val response = makeOrderResponse()

        whenever(bankIdRestService.startSign(ArgumentMatchers.anyString(), argumentCaptor.capture(), ArgumentMatchers.anyString())).thenReturn(response)

        sut.startSign(WebsignRequest(EMAIL, SSN, IP_ADDRESS), MEMBER_ID, false)

        assertThat(argumentCaptor.value).isEqualTo(NON_SWITCHER_MESSAGE)
    }

    @Test
    fun productSignConfirmed_givenSignSession_thenSetsStatusToComplete() {
        val session = makeSignSession(SignStatus.IN_PROGRESS)

        whenever(signSessionRepository.findByOrderReference(ORDER_REFERENCE))
            .thenReturn(Optional.of(session))

        sut.completeSession(ORDER_REFERENCE)

        assertThat(session.status).isEqualTo(SignStatus.COMPLETED)

        verify(signSessionRepository).save(session)
    }

    @Test
    fun startSign_givenBankidThrowsError_thenThrowException() {
        whenever(bankIdRestService.startSign(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.anyString()))
            .thenThrow(BankIdError::class.java)

        thrown.expect(BankIdError::class.java)

        sut.startSign(WebsignRequest(EMAIL, SSN, IP_ADDRESS), MEMBER_ID, false)
    }

    @Test
    fun startWebSign_givenSwitchingMember_thenSendSwitchingMessage() {
        val response = makeOrderResponse()

        whenever(bankIdRestService.startSign(ArgumentMatchers.anyString(), argumentCaptor.capture(), ArgumentMatchers.anyString())).thenReturn(response)

        sut.startSign(WebsignRequest(EMAIL, SSN, IP_ADDRESS), MEMBER_ID, true)

        assertThat(argumentCaptor.value).isEqualTo(SWITCHER_MESSAGE)
    }

    @Test
    fun startWebSign_givenMemberWithOkQuote_thenReturnOrderRefAndAutoStartToken() {
        whenever(bankIdRestService.startSign(ArgumentMatchers.matches(SSN), ArgumentMatchers.anyString(), ArgumentMatchers.anyString()))
            .thenReturn(makeOrderResponse())

        val response = sut.startSign(WebsignRequest(EMAIL, SSN, IP_ADDRESS), MEMBER_ID, true)

        assertThat(response.bankIdOrderResponse).hasFieldOrProperty("orderRef")
        assertThat(response.bankIdOrderResponse).hasFieldOrProperty("autoStartToken")
    }

    @Test
    fun startWebSign_givenReusableBankIdSession_thenDontCallBankIdReturnSignSession() {
        val signSession = Mockito.spy(SignSession::class.java)

        whenever(signSession.canReuseBankIdSession()).thenReturn(true)

        whenever(signSession.orderResponse).thenReturn(OrderResponse(ORDER_REFERENCE, AUTO_START_TOKEN))

        whenever(signSessionRepository.findByMemberId(MEMBER_ID)).thenReturn(
            Optional.of(signSession))

       val response = sut.startSign(WebsignRequest(EMAIL, SSN, IP_ADDRESS), MEMBER_ID, true)

        verify(bankIdRestService, never()).startSign(anyString(), anyString(), anyString())

        assertThat(response.bankIdOrderResponse!!.orderRef).isEqualTo(ORDER_REFERENCE)

        assertThat(response.bankIdOrderResponse!!.autoStartToken).isEqualTo(AUTO_START_TOKEN)
    }

     @Test
    fun getSignStatus_givenMatchingSignStatus_thenReturnSignStatus() {
        val session = makeSignSession(SignStatus.IN_PROGRESS)

        whenever(signSessionRepository.findByMemberId(MEMBER_ID))
            .thenReturn(Optional.of(session))

        val status = sut.getSignSession(MEMBER_ID)

        assertThat(status).get().isEqualTo(session)
    }

    @Test
    fun productSignConfirmed_givenNoSignSession_thenDoesNothing() {
        whenever(signSessionRepository.findByOrderReference(ORDER_REFERENCE)).thenReturn(Optional.empty())

        sut.completeSession(ORDER_REFERENCE)

        verify(signSessionRepository, never()).save(ArgumentMatchers.any())
    }

    private fun makeOrderResponse(): OrderResponse {
        return OrderResponse(
            ORDER_REFERENCE, AUTO_START_TOKEN)
    }

    private fun makeOrderResponse(orderReference: String, autostartToken: String): OrderResponse {
        return OrderResponse(
            orderReference, autostartToken)
    }

    fun makeCollectResponse(collectStatus: CollectStatus): CollectResponse {
        val userData = User(SSN, "Tolvan Tolvansson", "Tolvan", "Tolvansson")
        val device = Device(IP_ADDRESS)
        val cert = Cert(
            LocalDateTime.parse("2018-09-01T00:00:00").toInstant(
                ZoneOffset.UTC).toEpochMilli(),
            LocalDateTime.parse("2020-09-01T00:00:00").toInstant(
                ZoneOffset.UTC).toEpochMilli())
        val completionData = CompletionData(userData, device, cert, "", "")

        return CollectResponse(ORDER_REFERENCE, collectStatus,
            if (collectStatus == CollectStatus.complete) null else "someHint",
            if (collectStatus == CollectStatus.complete) completionData else null)
    }

    companion object {
        private const val MEMBER_ID = 1337L
        private const val SSN = "191212121212"
        private const val EMAIL = "test@test.com"
        private const val ORDER_REFERENCE = "orderReference"
        private const val AUTO_START_TOKEN = "autoStartToken"
        private const val ORDER_REFERENCE2 = "orderReference2"
        private const val AUTO_START_TOKEN2 = "autoStartToken2"
        private const val SWITCHER_MESSAGE = "SwitcherMessage"
        private const val NON_SWITCHER_MESSAGE = "NonSwitcherMessage"
        private const val IP_ADDRESS = "127.0.0.1"
        private fun makeSignSession(inProgress: SignStatus): SignSession {
            val session = SignSession(MEMBER_ID)
            session.newOrderStarted(OrderResponse(ORDER_REFERENCE, AUTO_START_TOKEN))
            session.status = inProgress
            return session
        }
    }
}
