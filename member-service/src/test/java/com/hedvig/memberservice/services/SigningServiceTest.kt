package com.hedvig.memberservice.services

import com.hedvig.external.bankID.bankIdTypes.Collect.Cert
import com.hedvig.external.bankID.bankIdTypes.Collect.Device
import com.hedvig.external.bankID.bankIdTypes.Collect.User
import com.hedvig.external.bankID.bankIdTypes.CollectResponse
import com.hedvig.external.bankID.bankIdTypes.CollectStatus
import com.hedvig.external.bankID.bankIdTypes.CompletionData
import com.hedvig.external.bankID.bankIdTypes.OrderResponse
import com.hedvig.integration.botService.BotService
import com.hedvig.integration.underwriter.UnderwriterApi
import com.hedvig.integration.underwriter.dtos.QuoteToSignStatusDto
import com.hedvig.integration.underwriter.dtos.SignMethod
import com.hedvig.memberservice.commands.UpdateWebOnBoardingInfoCommand
import com.hedvig.memberservice.entities.SignSession
import com.hedvig.memberservice.entities.SignStatus
import com.hedvig.memberservice.query.MemberEntity
import com.hedvig.memberservice.query.MemberRepository
import com.hedvig.memberservice.query.SignedMemberEntity
import com.hedvig.memberservice.query.SignedMemberRepository
import com.hedvig.memberservice.services.member.CannotSignInsuranceException
import com.hedvig.memberservice.services.member.dto.MemberSignResponse
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
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when` as whenever
import org.mockito.junit.MockitoJUnitRunner
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.*

@RunWith(MockitoJUnitRunner::class)
class SigningServiceTest {

    @Mock
    lateinit var underwriterApi: UnderwriterApi

    @Mock
    lateinit var signedMemberRepository: SignedMemberRepository

    @Mock
    lateinit var memberRepository: MemberRepository

    @Mock
    lateinit var botService: BotService

    @Mock
    lateinit var commandGateway: CommandGateway

    @Mock
    lateinit var swedishBankIdSigningService: SwedishBankIdSigningService

    @Mock
    lateinit var norwegianSigningService: NorwegianSigningService

    @Rule @JvmField
    var thrown = ExpectedException.none()

    @Captor
    lateinit var updateWebOnBoardingInfoCommandArgumentCaptor: ArgumentCaptor<UpdateWebOnBoardingInfoCommand>

    private lateinit var sut: SigningService

    @Before
    fun setup() {
        whenever(signedMemberRepository.findBySsn(ArgumentMatchers.any())).thenReturn(Optional.empty())

        sut = SigningService(underwriterApi, signedMemberRepository, botService, memberRepository, commandGateway,
            swedishBankIdSigningService, norwegianSigningService)
    }

    @Test
    @Throws(CannotSignInsuranceException::class)
    fun startWebSign_givenMemberWithoutOkQuote_thenThrowException() {
        whenever(underwriterApi.hasQuoteToSign(MEMBER_ID.toString())).thenReturn(
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
    fun getSignStatus_givenNoMatchingSignStatus_thenReturnEmpty() {
        val status = sut.getSignStatus(MEMBER_ID)

        assertThat(status).isNull()
    }

    @Test
    fun productSignConfirmed_whenBotserviceThrowsException_Continues() {
        whenever(memberRepository.getOne(MEMBER_ID)).thenReturn(MemberEntity())

        BDDMockito.willThrow(RuntimeException::class.java).given(botService).initBotServiceSessionWebOnBoarding(ArgumentMatchers.anyLong(), ArgumentMatchers.any())

        sut.productSignConfirmed(MEMBER_ID)
    }

    @Test
    fun startWebSign_sendsUpdateWebOnBoardingInfoCommand() {
        whenever(underwriterApi.hasQuoteToSign(MEMBER_ID.toString())).thenReturn(
            makeQuoteToSignStatusEligibleSwitching())

        whenever(swedishBankIdSigningService.startSign(WebsignRequest(EMAIL, SSN, IP_ADDRESS), MEMBER_ID,  true))
            .thenReturn(MemberSignResponse(
                MEMBER_ID,
                SignStatus.INITIATED,
                OrderResponse(
                    ORDER_REFERENCE,
                    AUTO_START_TOKEN
                )
            ))

        val (_, _, _, bankIdOrderResponse) = sut.startWebSign(MEMBER_ID, WebsignRequest(EMAIL, SSN, IP_ADDRESS))

        verify(commandGateway).sendAndWait<Any>(updateWebOnBoardingInfoCommandArgumentCaptor.capture())

        assertThat(updateWebOnBoardingInfoCommandArgumentCaptor.value.email).isNotEmpty()
    }

    private fun makeQuoteToSignStatusEligibleSwitching(): QuoteToSignStatusDto {
        return QuoteToSignStatusDto.EligibleToSign(true, SignMethod.SWEDISH_BANK_ID)
    }

    private fun makeQuoteToSignStatusEligibleNotSwitching(): QuoteToSignStatusDto {
        return QuoteToSignStatusDto.EligibleToSign(false, SignMethod.SWEDISH_BANK_ID)
    }

    private fun makeQuoteToSignStatusNotEligibleSwitching(): QuoteToSignStatusDto {
        return QuoteToSignStatusDto.NotEligibleToSign
    }

    companion object {
        private const val MEMBER_ID = 1337L
        private const val SSN = "191212121212"
        private const val EMAIL = "test@test.com"
        private const val ORDER_REFERENCE = "orderReference"
        private const val AUTO_START_TOKEN = "autoStartToken"
        private const val IP_ADDRESS = "127.0.0.1"
        private fun makeSignSession(inProgress: SignStatus): SignSession {
            val session = SignSession(MEMBER_ID)
            session.newOrderStarted(OrderResponse(ORDER_REFERENCE, AUTO_START_TOKEN))
            session.status = inProgress
            return session
        }
    }
}
