package com.hedvig.memberservice.services

import com.hedvig.external.bankID.bankIdTypes.BankIdError
import com.hedvig.external.bankID.bankIdTypes.Collect.Cert
import com.hedvig.external.bankID.bankIdTypes.Collect.Device
import com.hedvig.external.bankID.bankIdTypes.Collect.User
import com.hedvig.external.bankID.bankIdTypes.CollectResponse
import com.hedvig.external.bankID.bankIdTypes.CollectStatus
import com.hedvig.external.bankID.bankIdTypes.CompletionData
import com.hedvig.external.bankID.bankIdTypes.OrderResponse
import com.hedvig.integration.botService.BotService
import com.hedvig.integration.underwritter.UnderwriterApi
import com.hedvig.integration.underwritter.dtos.QuoteToSignStatusDTO
import com.hedvig.memberservice.commands.UpdateWebOnBoardingInfoCommand
import com.hedvig.memberservice.entities.SignSession
import com.hedvig.memberservice.entities.SignSessionRepository
import com.hedvig.memberservice.entities.SignStatus
import com.hedvig.memberservice.query.MemberEntity
import com.hedvig.memberservice.query.MemberRepository
import com.hedvig.memberservice.query.SignedMemberEntity
import com.hedvig.memberservice.query.SignedMemberRepository
import com.hedvig.memberservice.services.member.CannotSignInsuranceException
import com.hedvig.memberservice.services.member.MemberService
import com.hedvig.memberservice.web.v2.dto.WebsignRequest
import org.assertj.core.api.Assertions.assertThat
import org.axonframework.commandhandling.gateway.CommandGateway
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExpectedException
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers
import org.mockito.BDDMockito
import org.mockito.Captor
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoMoreInteractions
import org.mockito.Mockito.verifyZeroInteractions
import org.mockito.Mockito.`when` as whenever
import org.mockito.junit.MockitoJUnitRunner
import org.quartz.JobDetail
import org.quartz.Scheduler
import org.quartz.SchedulerException
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.*

@RunWith(MockitoJUnitRunner::class)
class SigningServiceTest {

    @Mock
    lateinit var underwriterApi: UnderwriterApi

    @Mock
    lateinit var bankIdRestService: BankIdRestService

    @Mock
    lateinit var norwegianBankIdService: NorwegianBankIdService

    @Mock
    lateinit var signedMemberRepository: SignedMemberRepository

    @Mock
    lateinit var signSessionRepository: SignSessionRepository

    @Mock
    lateinit var scheduler: Scheduler

    @Mock
    lateinit var memberService: MemberService

    @Mock
    lateinit var memberRepository: MemberRepository

    @Mock
    lateinit var botService: BotService

    @Mock
    lateinit var commandGateway: CommandGateway

    @Rule @JvmField
    var thrown = ExpectedException.none()

    @Captor
    lateinit var jobDetailArgumentCaptor: ArgumentCaptor<JobDetail>

    @Captor
    lateinit var argumentCaptor: ArgumentCaptor<String>

    @Captor
    lateinit var updateWebOnBoardingInfoCommandArgumentCaptor: ArgumentCaptor<UpdateWebOnBoardingInfoCommand>

    private lateinit var sut: SigningService

    private lateinit var swedishBankIdSigningService: SwedishBankIdSigningService

    private lateinit var norwegianSigningService: NorwegianSigningService

    @Before
    fun setup() {
        whenever(signedMemberRepository.findBySsn(ArgumentMatchers.any())).thenReturn(Optional.empty())

        swedishBankIdSigningService = SwedishBankIdSigningService(
            bankIdRestService, signSessionRepository, memberRepository, botService,
            scheduler, memberService, SWITCHER_MESSAGE, NON_SWITCHER_MESSAGE
        )
        norwegianSigningService = NorwegianSigningService(
            memberRepository, norwegianBankIdService
        )
        sut = SigningService(underwriterApi, signedMemberRepository, memberRepository, commandGateway,
            swedishBankIdSigningService, norwegianSigningService)
    }

    @Test
    fun startWebSign_givenMemberWithOkQuote_thenReturnOrderRefAndAutoStartToken() {
        whenever(underwriterApi.hasQuoteToSign(java.lang.Long.toString(MEMBER_ID))).thenReturn(
            makeQuoteToSignStatusEligibleSwitching())

        whenever(bankIdRestService.startSign(ArgumentMatchers.matches(SSN), ArgumentMatchers.anyString(), ArgumentMatchers.anyString()))
            .thenReturn(makeOrderResponse())

        val (_, _, _, bankIdOrderResponse) = sut.startWebSign(MEMBER_ID, WebsignRequest(EMAIL, SSN, IP_ADDRESS))


        assertThat(bankIdOrderResponse).hasFieldOrProperty("orderRef")
        assertThat(bankIdOrderResponse).hasFieldOrProperty("autoStartToken")
    }

    @Test
    @Throws(SchedulerException::class)
    fun startWebSign_givenMemberWithOkQuote_thenSchedulesCollectJob() {
        whenever(underwriterApi.hasQuoteToSign(java.lang.Long.toString(MEMBER_ID))).thenReturn(
            makeQuoteToSignStatusEligibleSwitching())

        whenever(bankIdRestService.startSign(ArgumentMatchers.matches(SSN), ArgumentMatchers.anyString(), ArgumentMatchers.anyString()))
            .thenReturn(makeOrderResponse())

        whenever(scheduler.scheduleJob(jobDetailArgumentCaptor.capture(), ArgumentMatchers.any())).thenReturn(Date.from(
            Instant.now()))

        sut.startWebSign(MEMBER_ID, WebsignRequest(EMAIL, SSN, IP_ADDRESS))

        verify(scheduler).scheduleJob(ArgumentMatchers.any(), ArgumentMatchers.any())
        assertThat(jobDetailArgumentCaptor.value.key.name).isEqualTo(ORDER_REFERENCE)
    }

    @Test
    fun startWebSign_givenMemberWithoutOkQuote_thenThrowException() {
        whenever(underwriterApi.hasQuoteToSign(java.lang.Long.toString(MEMBER_ID))).thenReturn(
            makeQuoteToSignStatusNotEligibleSwitching())

        thrown.expect(CannotSignInsuranceException::class.java)

        sut.startWebSign(MEMBER_ID, WebsignRequest(EMAIL, SSN, IP_ADDRESS))
    }

    @Test
    fun startWebSign_givenMemberInSignedMemberEntity_thenThrowException() {
        val memberId = MEMBER_ID
        val memberEntity = SignedMemberEntity()
        memberEntity.id = memberId
        memberEntity.ssn = SSN

        whenever(signedMemberRepository.findBySsn(SSN)).thenReturn(Optional.of(memberEntity))

        thrown.expect(MemberHasExistingInsuranceException::class.java)

        sut.startWebSign(memberId, WebsignRequest(EMAIL, SSN, IP_ADDRESS))
    }

    @Test
    fun startWebSign_givenBankidThrowsError_thenThrowException() {
        whenever(underwriterApi.hasQuoteToSign(java.lang.Long.toString(MEMBER_ID))).thenReturn(
            makeQuoteToSignStatusEligibleSwitching())

        whenever(bankIdRestService.startSign(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.anyString()))
            .thenThrow(BankIdError::class.java)

        thrown.expect(BankIdError::class.java)

        sut.startWebSign(MEMBER_ID, WebsignRequest(EMAIL, SSN, IP_ADDRESS))
    }

    @Test
    fun startWebSign_givenSwitchingMember_thenSendSwitchingMessage() {
        whenever(underwriterApi.hasQuoteToSign(java.lang.Long.toString(MEMBER_ID))).thenReturn(
            makeQuoteToSignStatusEligibleSwitching())

        val response = makeOrderResponse()

        whenever(bankIdRestService.startSign(ArgumentMatchers.anyString(), argumentCaptor.capture(), ArgumentMatchers.anyString())).thenReturn(response)

        sut.startWebSign(MEMBER_ID, WebsignRequest(EMAIL, SSN, IP_ADDRESS))

        assertThat(argumentCaptor.value).isEqualTo(SWITCHER_MESSAGE)
    }

    @Test
    fun startWebSign_givenNonSwitchingMember_thenSendNonSwitchingMessage() {
        whenever(underwriterApi.hasQuoteToSign(java.lang.Long.toString(MEMBER_ID))).thenReturn(
            makeQuoteToSignStatusEligibleNotSwitching())

        val response = makeOrderResponse()

        whenever(bankIdRestService.startSign(ArgumentMatchers.anyString(), argumentCaptor.capture(), ArgumentMatchers.anyString())).thenReturn(response)

        sut.startWebSign(MEMBER_ID, WebsignRequest(EMAIL, SSN, IP_ADDRESS))

        assertThat(argumentCaptor.value).isEqualTo(NON_SWITCHER_MESSAGE)
    }

    @Test
    fun startWebSign_givenReusableBankIdSession_thenDontCallBankIdReturnSignSession() {
        whenever(underwriterApi.hasQuoteToSign(java.lang.Long.toString(MEMBER_ID))).thenReturn(
            makeQuoteToSignStatusEligibleNotSwitching())

        val signSession = Mockito.spy(SignSession::class.java)

        whenever(signSession.canReuseBankIdSession()).thenReturn(true)

        whenever(signSession.orderResponse).thenReturn(OrderResponse(ORDER_REFERENCE, AUTO_START_TOKEN))

        whenever(signSessionRepository.findByMemberId(MEMBER_ID)).thenReturn(
            Optional.of(signSession))

        val (_, _, _, bankIdOrderResponse) = sut.startWebSign(MEMBER_ID, WebsignRequest(EMAIL, SSN, IP_ADDRESS))

        verify(bankIdRestService, never()).startSign(ArgumentMatchers.anyString(), ArgumentMatchers.anyString(), ArgumentMatchers.anyString())

        assertThat(bankIdOrderResponse!!.orderRef).isEqualTo(ORDER_REFERENCE)

        assertThat(bankIdOrderResponse.autoStartToken).isEqualTo(AUTO_START_TOKEN)

        verify(commandGateway).sendAndWait<Any>(updateWebOnBoardingInfoCommandArgumentCaptor.capture())

        assertThat(updateWebOnBoardingInfoCommandArgumentCaptor.value.email).isNotEmpty()
    }

    @Test
    fun startWebSign_givenNonReusableBankIdSession_thenCallBankIdReturnOrderRefAndAutoStartToken() {
        whenever(underwriterApi.hasQuoteToSign(java.lang.Long.toString(MEMBER_ID))).thenReturn(
            makeQuoteToSignStatusEligibleSwitching())

        val signSession = Mockito.mock(SignSession::class.java)

        whenever(signSession.canReuseBankIdSession()).thenReturn(false)

        whenever(signSessionRepository.findByMemberId(MEMBER_ID)).thenReturn(Optional.of(signSession))

        whenever(bankIdRestService.startSign(ArgumentMatchers.matches(SSN), ArgumentMatchers.anyString(), ArgumentMatchers.anyString()))
            .thenReturn(makeOrderResponse(ORDER_REFERENCE2, AUTO_START_TOKEN2))

        val (_, _, _, bankIdOrderResponse) = sut.startWebSign(MEMBER_ID, WebsignRequest(EMAIL, SSN, IP_ADDRESS))

        assertThat(bankIdOrderResponse).hasFieldOrPropertyWithValue("orderRef", ORDER_REFERENCE2)

        assertThat(bankIdOrderResponse).hasFieldOrPropertyWithValue("autoStartToken",
            AUTO_START_TOKEN2)

        verify(commandGateway).sendAndWait<Any>(updateWebOnBoardingInfoCommandArgumentCaptor.capture())

        assertThat(updateWebOnBoardingInfoCommandArgumentCaptor.value.email).isNotEmpty()
    }

    @Test
    fun collectBankId_givenSessionInProgress_thenReturnTrue() {
        val session = makeSignSession(SignStatus.IN_PROGRESS)

        whenever(signSessionRepository.findByOrderReference(ORDER_REFERENCE))
            .thenReturn(Optional.of(session))

        val response = makeCollectResponse(CollectStatus.pending)

        whenever(bankIdRestService.collect(ORDER_REFERENCE)).thenReturn(response)

        val actual = swedishBankIdSigningService.collectBankId(ORDER_REFERENCE)

        assertThat(actual).isEqualTo(true)
    }

    @Test
    fun collectBankId_givenSessionThatIsComplete_thenReturnFalse() {

        val session = makeSignSession(SignStatus.COMPLETED)

        whenever(signSessionRepository.findByOrderReference(ORDER_REFERENCE))
            .thenReturn(Optional.of(session))

        val status = swedishBankIdSigningService.collectBankId(ORDER_REFERENCE)

        assertThat(status).isEqualTo(false)

        verifyZeroInteractions(bankIdRestService)
    }

    @Test
    fun collectBankId_givenSessionThatIsFailed_thenReturnFalse() {

        val session = makeSignSession(SignStatus.FAILED)

        whenever(signSessionRepository.findByOrderReference(ORDER_REFERENCE))
            .thenReturn(Optional.of(session))

        val status = swedishBankIdSigningService.collectBankId(ORDER_REFERENCE)

        assertThat(status).isEqualTo(false)

        verifyZeroInteractions(bankIdRestService)
    }

    @Test
    fun collectBankId_givenNoMatchingOrderReference_thenReturnFalse() {

        whenever(signSessionRepository.findByOrderReference(ORDER_REFERENCE)).thenReturn(Optional.empty())

        val status = swedishBankIdSigningService.collectBankId(ORDER_REFERENCE)

        assertThat(status).isEqualTo(false)

        verifyZeroInteractions(bankIdRestService)
    }

    @Test
    fun collectBankId_givenBankIdReturnPending_thenReturnTrue() {

        val session = makeSignSession(SignStatus.IN_PROGRESS)

        whenever(signSessionRepository.findByOrderReference(ORDER_REFERENCE))
            .thenReturn(Optional.of(session))

        val response = makeCollectResponse(CollectStatus.pending)

        whenever(bankIdRestService.collect(ArgumentMatchers.eq(ORDER_REFERENCE))).thenReturn(response)

        val status = swedishBankIdSigningService.collectBankId(ORDER_REFERENCE)

        assertThat(status).isEqualTo(true)

        assertThat(session.collectResponse)
            .hasFieldOrPropertyWithValue("status", CollectStatus.pending)

        verify(signSessionRepository).save(ArgumentMatchers.eq(session))
    }

    @Test
    fun collectBankId_givenBankIdReturnComplete_thenReturnFalse() {

        val session = makeSignSession(SignStatus.IN_PROGRESS)

        whenever(signSessionRepository.findByOrderReference(ORDER_REFERENCE))
            .thenReturn(Optional.of(session))

        val response = makeCollectResponse(CollectStatus.complete)

        whenever(bankIdRestService.collect(ArgumentMatchers.eq(ORDER_REFERENCE))).thenReturn(response)

        val status = swedishBankIdSigningService.collectBankId(ORDER_REFERENCE)

        assertThat(status).isEqualTo(false)

        assertThat(session.collectResponse)
            .hasFieldOrPropertyWithValue("status", CollectStatus.complete)

        verify(signSessionRepository).save(ArgumentMatchers.eq(session))
    }

    @Test
    fun collectBankId_givenBankIdReturnComplete_thenSendBankIdCompleteToMemberService() {

        val session = makeSignSession(SignStatus.IN_PROGRESS)

        whenever(signSessionRepository.findByOrderReference(ORDER_REFERENCE))
            .thenReturn(Optional.of(session))

        val response = makeCollectResponse(CollectStatus.complete)

        whenever(bankIdRestService.collect(ArgumentMatchers.eq(ORDER_REFERENCE))).thenReturn(response)

        swedishBankIdSigningService.collectBankId(ORDER_REFERENCE)

        verify(memberService).bankIdSignComplete(ArgumentMatchers.eq(MEMBER_ID), ArgumentMatchers.eq(response))
    }

    @Test
    fun collectBankId_givenBankIdReturnComplete_thenSetsSignStatusToComplete() {

        val session = makeSignSession(SignStatus.IN_PROGRESS)

        whenever(signSessionRepository.findByOrderReference(ORDER_REFERENCE))
            .thenReturn(Optional.of(session))

        val response = makeCollectResponse(CollectStatus.complete)

        whenever(bankIdRestService.collect(ArgumentMatchers.eq(ORDER_REFERENCE))).thenReturn(response)

        swedishBankIdSigningService.collectBankId(ORDER_REFERENCE)

        assertThat(session.status).isEqualTo(SignStatus.COMPLETED)
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

    @Test
    fun collectBankId_givenBankIdReturnFailed_thenSetsSignStatusToFailed() {
        val session = makeSignSession(SignStatus.IN_PROGRESS)

        whenever(signSessionRepository.findByOrderReference(ORDER_REFERENCE))
            .thenReturn(Optional.of(session))

        val response = makeCollectResponse(CollectStatus.failed)

        whenever(bankIdRestService.collect(ArgumentMatchers.eq(ORDER_REFERENCE))).thenReturn(response)

        swedishBankIdSigningService.collectBankId(ORDER_REFERENCE)

        assertThat(session.status).isEqualTo(SignStatus.FAILED)
    }

    @Test
    fun getSignStatus_givenNoMatchingSignStatus_thenReturnEmpty() {
        val status = sut.getSignStatus(MEMBER_ID)

        assertThat(status).isEmpty
    }

    @Test
    fun getSignStatus_givenMatchingSignStatus_thenReturnSignStatus() {
        val session = makeSignSession(SignStatus.IN_PROGRESS)

        whenever(signSessionRepository.findByMemberId(MEMBER_ID))
            .thenReturn(Optional.of(session))

        whenever(memberRepository.findById(MEMBER_ID))
            .thenReturn(Optional.of(MemberEntity.builder().ssn(SSN).build()))

        val status = sut.getSignStatus(MEMBER_ID)

        assertThat(status).get().isEqualTo(session)
    }

    @Test
    fun productSignConfirmed_givenSignSession_thenSetsStatusToComplete() {
        val session = makeSignSession(SignStatus.IN_PROGRESS)

        whenever(signSessionRepository.findByOrderReference(ORDER_REFERENCE))
            .thenReturn(Optional.of(session))
        whenever(memberRepository.getOne(ArgumentMatchers.anyLong())).thenReturn(MemberEntity())

        sut.productSignConfirmed(SSN, ORDER_REFERENCE)

        assertThat(session.status).isEqualTo(SignStatus.COMPLETED)

        verify(signSessionRepository).save(session)
    }

    @Test
    fun productSignConfirmed_givenNoSignSession_thenDoesNothing() {
        whenever(signSessionRepository.findByOrderReference(ORDER_REFERENCE)).thenReturn(Optional.empty())

        sut.productSignConfirmed(SSN, ORDER_REFERENCE)

        verify(signSessionRepository, never()).save(ArgumentMatchers.any())
    }

    @Test
    fun productSignConfirmed_whenBotserviceThrowsException_Continues() {
        val session = makeSignSession(SignStatus.IN_PROGRESS)

        whenever(signSessionRepository.findByOrderReference(ORDER_REFERENCE))
            .thenReturn(Optional.of(session))

        whenever(memberRepository.getOne(ArgumentMatchers.anyLong())).thenReturn(MemberEntity())

        BDDMockito.willThrow(RuntimeException::class.java).given(botService).initBotServiceSessionWebOnBoarding(ArgumentMatchers.anyLong(), ArgumentMatchers.any())

        sut.productSignConfirmed(SSN, ORDER_REFERENCE)
    }

    private fun makeOrderResponse(): OrderResponse {
        return OrderResponse(
            ORDER_REFERENCE, AUTO_START_TOKEN)
    }

    private fun makeOrderResponse(orderReference: String, autostartToken: String): OrderResponse {
        return OrderResponse(
            orderReference, autostartToken)
    }

    private fun makeQuoteToSignStatusEligibleSwitching(): QuoteToSignStatusDTO {
        return QuoteToSignStatusDTO(true, true)
    }

    private fun makeQuoteToSignStatusNotEligibleSwitching(): QuoteToSignStatusDTO {
        return QuoteToSignStatusDTO(false, true)
    }

    private fun makeQuoteToSignStatusEligibleNotSwitching(): QuoteToSignStatusDTO {
        return QuoteToSignStatusDTO(true, false)
    }

    companion object {
        private const val MEMBER_ID = 1337L
        private const val SSN = "191212121212"
        private const val EMAIL = "test@test.com"
        private const val ORDER_REFERENCE = "1337"
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
