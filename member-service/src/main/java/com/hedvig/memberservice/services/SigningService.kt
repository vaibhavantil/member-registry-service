package com.hedvig.memberservice.services

import com.hedvig.external.bankID.bankIdTypes.BankIdError
import com.hedvig.external.bankID.bankIdTypes.CollectStatus
import com.hedvig.external.bankID.bankIdTypes.OrderResponse
import com.hedvig.integration.botService.BotService
import com.hedvig.integration.botService.dto.UpdateUserContextDTO
import com.hedvig.integration.productsPricing.ProductApi
import com.hedvig.memberservice.commands.SignMemberFromUnderwriterCommand
import com.hedvig.memberservice.commands.UpdateWebOnBoardingInfoCommand
import com.hedvig.memberservice.entities.CollectResponse
import com.hedvig.memberservice.entities.SignSession
import com.hedvig.memberservice.entities.SignSessionRepository
import com.hedvig.memberservice.entities.SignStatus
import com.hedvig.memberservice.jobs.BankIdCollector
import com.hedvig.memberservice.query.MemberRepository
import com.hedvig.memberservice.query.SignedMemberRepository
import com.hedvig.memberservice.services.member.CannotSignInsuranceException
import com.hedvig.memberservice.services.member.MemberService
import com.hedvig.memberservice.services.member.dto.MemberSignResponse
import com.hedvig.memberservice.services.member.dto.MemberSignUnderwriterQuoteResponse
import com.hedvig.memberservice.web.dto.IsSsnAlreadySignedMemberResponse
import com.hedvig.memberservice.web.v2.dto.UnderwriterQuoteSignRequest
import com.hedvig.memberservice.web.v2.dto.WebsignRequest
import org.axonframework.commandhandling.gateway.CommandGateway
import org.quartz.JobBuilder
import org.quartz.Scheduler
import org.quartz.SchedulerException
import org.quartz.SimpleScheduleBuilder
import org.quartz.TriggerBuilder
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.lang.NonNull
import org.springframework.stereotype.Service
import java.util.*
import javax.transaction.Transactional

@Service
class SigningService(
    private val bankidService: BankIdRestService,
    private val productApi: ProductApi,
    private val signedMemberRepository: SignedMemberRepository,
    private val signSessionRepository: SignSessionRepository,
    private val scheduler: Scheduler,
    private val memberService: MemberService,
    private val memberRepository: MemberRepository,
    private val botService: BotService,
    private val commandGateway: CommandGateway,
    @Value("\${hedvig.bankid.signmessage.switcher}")
    private val switcherMessage: String,
    @Value("\${hedvig.bankid.signmessage.nonSwitcher}")
    private val nonSwitcherMessage: String
) {

    @Transactional
    fun startWebSign(memberId: Long, request: WebsignRequest): MemberSignResponse {

        val existing = signedMemberRepository.findBySsn(request.ssn)

        if (existing.isPresent) {
            throw MemberHasExistingInsuranceException()
        }

        val productStatus = productApi.hasProductToSign(memberId)
        return if (productStatus.isEligibleToSign) {

            val session = signSessionRepository.findByMemberId(memberId).orElseGet { SignSession(memberId) }

            val cmd = UpdateWebOnBoardingInfoCommand(memberId,
                request.ssn, request.email)
            commandGateway.sendAndWait<Any>(cmd)

            if (!session.canReuseBankIdSession()) {

                // TODO: on ssn figure out if we should use norwegian sign
                val result = bankidService.startSign(
                    request.ssn,
                    createUserSignText(productStatus.isSwitching),
                    request.ipAddress)
                session.newOrderStarted(result)
                signSessionRepository.save(session)
                scheduleCollectJob(result)
                return MemberSignResponse(session.sessionId, SignStatus.IN_PROGRESS, result)
            }
            MemberSignResponse(session.sessionId, SignStatus.IN_PROGRESS,
                session.orderResponse)
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

    @Transactional
    fun scheduleCollectJob(result: OrderResponse) {
        try {
            val jobName = result.orderRef
            val jobDetail = JobBuilder.newJob()
                .withIdentity(jobName, "bankid.collect")
                .ofType(BankIdCollector::class.java)
                .build()
            val trigger = TriggerBuilder.newTrigger()
                .forJob(jobName, "bankid.collect")
                .withSchedule(SimpleScheduleBuilder.simpleSchedule().withIntervalInSeconds(1).withRepeatCount(900)
                    .withMisfireHandlingInstructionNowWithRemainingCount())
                .build()
            scheduler.scheduleJob(jobDetail,
                trigger)
        } catch (e: SchedulerException) {
            throw RuntimeException(e.message, e)
        }
    }

    /**
     * @param orderReference order reference from bankIdq
     * @return true if BankID needs to be collected again, otherwise false
     */
    @Transactional
    fun collectBankId(@NonNull orderReference: String?): Boolean {
        val session = signSessionRepository.findByOrderReference(orderReference)
        return session
            .map { s: SignSession ->
                if (s.status == SignStatus.IN_PROGRESS) {
                    try {
                        val response = bankidService.collect(orderReference)
                        val collectResponse = CollectResponse()
                        collectResponse.status = response.status
                        collectResponse.hintCode = response.hintCode
                        s.newCollectResponse(collectResponse)
                        if (response.status == CollectStatus.complete) {
                            memberService.bankIdSignComplete(s.memberId, response)
                            s.status = SignStatus.COMPLETED
                        } else if (response.status == CollectStatus.failed) {
                            s.status = SignStatus.FAILED
                        }
                        signSessionRepository.save(s)
                        return@map response.status == CollectStatus.pending
                    } catch (e: BankIdError) {
                        s.status = SignStatus.FAILED
                        signSessionRepository.save(s)
                    }
                }
                false
            }
            .orElseGet {
                log.error("Could not find SignSession with orderReference: ", orderReference)
                false
            }
    }

    fun getSignStatus(@NonNull orderRef: Long): Optional<SignSession> {
        return signSessionRepository.findByMemberId(orderRef)
    }

    @Transactional
    fun productSignConfirmed(id: String?) {
        val session = signSessionRepository.findByOrderReference(id)
        session.ifPresent { s: SignSession ->
            s.status = SignStatus.COMPLETED
            signSessionRepository.save(s)
            val member = memberRepository.getOne(s.memberId)
            val userContext = UpdateUserContextDTO(
                s.memberId.toString(),
                member.getSsn(),
                member.getFirstName(),
                member.getLastName(),
                member.getPhoneNumber(),
                member.getEmail(),
                member.getStreet(),
                member.getCity(),
                member.zipCode,
                true)
            try {
                botService.initBotServiceSessionWebOnBoarding(s.memberId, userContext)
            } catch (ex: RuntimeException) {
                log.error("Could not initialize bot-service for memberId: {}", s.memberId, ex)
            }
        }
    }

    private fun createUserSignText(isSwitching: Boolean): String {
        val signText: String
        signText = if (isSwitching) {
            switcherMessage
        } else {
            nonSwitcherMessage
        }
        return signText
    }

    companion object {
        private val log = LoggerFactory.getLogger(SigningService::class.java)
    }

}
