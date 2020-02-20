package com.hedvig.memberservice.services

import com.hedvig.integration.botService.BotService
import com.hedvig.integration.underwritter.UnderwriterApi
import com.hedvig.memberservice.commands.SignMemberFromUnderwriterCommand
import com.hedvig.memberservice.commands.UpdateWebOnBoardingInfoCommand
import com.hedvig.memberservice.entities.SignSession
import com.hedvig.memberservice.query.MemberRepository
import com.hedvig.memberservice.query.SignedMemberRepository
import com.hedvig.memberservice.services.member.CannotSignInsuranceException
import com.hedvig.memberservice.services.member.dto.MemberSignResponse
import com.hedvig.memberservice.services.member.dto.MemberSignUnderwriterQuoteResponse
import com.hedvig.memberservice.util.Market
import com.hedvig.memberservice.util.SsnUtilImpl
import com.hedvig.memberservice.web.dto.IsSsnAlreadySignedMemberResponse
import com.hedvig.memberservice.web.v2.dto.UnderwriterQuoteSignRequest
import com.hedvig.memberservice.web.v2.dto.WebsignRequest
import org.axonframework.commandhandling.gateway.CommandGateway
import org.slf4j.LoggerFactory
import org.springframework.lang.NonNull
import org.springframework.stereotype.Service
import java.util.*
import javax.transaction.Transactional

@Service
class SigningService(
    private val underwriterApi: UnderwriterApi,
    private val signedMemberRepository: SignedMemberRepository,
    private val botService: BotService,
    private val memberRepository: MemberRepository,
    private val commandGateway: CommandGateway,
    private val swedishBankIdSigningService: SwedishBankIdSigningService,
    private val norwegianSigningService: NorwegianSigningService
) {

    @Transactional
    fun startWebSign(memberId: Long, request: WebsignRequest): MemberSignResponse {

        val existing = signedMemberRepository.findBySsn(request.ssn)

        if (existing.isPresent) {
            throw MemberHasExistingInsuranceException()
        }

        val quoteStatus = underwriterApi.hasQuoteToSign(memberId.toString())
        if (quoteStatus.isEligibleToSign) {
            val cmd = UpdateWebOnBoardingInfoCommand(memberId, request.ssn, request.email)
            commandGateway.sendAndWait<Any>(cmd)

            return when (SsnUtilImpl.instance.getMarketFromSsn(request.ssn)) {
                Market.SWEDEN -> swedishBankIdSigningService.startSign(request, memberId, quoteStatus.isSwitching)
                Market.NORWAY -> norwegianSigningService.startSign(memberId, request)
            }
        } else {
            throw CannotSignInsuranceException()
        }
    }

    @Transactional
    fun signUnderwriterQuote(memberId: Long, request: UnderwriterQuoteSignRequest): MemberSignUnderwriterQuoteResponse {
        val existing = signedMemberRepository.findBySsn(request.ssn)

        if (existing.isPresent) {
            throw MemberHasExistingInsuranceException()
        }
        return try {
            val signMember = commandGateway.send<Any>(
                SignMemberFromUnderwriterCommand(memberId, request.ssn))
            MemberSignUnderwriterQuoteResponse(memberId, signMember.isDone)
        } catch (exception: Exception) {
            throw CannotSignInsuranceException()
        }
    }

    fun IsSsnAlreadySignedMember(ssn: String?): IsSsnAlreadySignedMemberResponse {
        val existing = signedMemberRepository.findBySsn(ssn)
        return IsSsnAlreadySignedMemberResponse(existing.isPresent)
    }

    fun getSignStatus(@NonNull memberId: Long): Optional<SignSession> {
        val optionalMember = memberRepository.findById(memberId)

        return if (optionalMember.isPresent) {
            when (SsnUtilImpl.instance.getMarketFromSsn(optionalMember.get().ssn)) {
                Market.SWEDEN -> swedishBankIdSigningService.getSignStatus(memberId)
                Market.NORWAY -> TODO()
            }
        } else {
            Optional.empty()
        }
    }

    @Transactional
    fun productSignConfirmed(ssn: String, id: String?) {
        val userContext = when (SsnUtilImpl.instance.getMarketFromSsn(ssn)) {
            Market.SWEDEN -> swedishBankIdSigningService.getUserContextDTOFromSession(id)
            Market.NORWAY -> TODO()
        }

        userContext?.let {
            val memberId = it.memberId.toLong()
            try {
                botService.initBotServiceSessionWebOnBoarding(memberId, it)
            } catch (ex: RuntimeException) {
                log.error("Could not initialize bot-service for memberId: {}", memberId, ex)
            }
        }
    }

    companion object {
        private val log = LoggerFactory.getLogger(SigningService::class.java)
    }
}
