package com.hedvig.memberservice.services.signing.sweden

import com.hedvig.external.bankID.bankIdTypes.BankIdError
import com.hedvig.external.bankID.bankIdTypes.CollectStatus
import com.hedvig.external.bankID.bankIdTypes.OrderResponse
import com.hedvig.memberservice.entities.CollectResponse
import com.hedvig.memberservice.entities.SignSession
import com.hedvig.memberservice.entities.SignSessionRepository
import com.hedvig.memberservice.entities.SignStatus
import com.hedvig.memberservice.jobs.BankIdCollector
import com.hedvig.memberservice.jobs.SwedishBankIdMetrics
import com.hedvig.memberservice.services.BankIdRestService
import com.hedvig.memberservice.services.member.MemberService
import com.hedvig.memberservice.services.member.dto.MemberSignResponse
import com.hedvig.memberservice.services.member.dto.StartSwedishSignResponse
import com.hedvig.memberservice.util.logger
import com.hedvig.memberservice.web.v2.dto.WebsignRequest
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
class SwedishBankIdSigningService(
    private val bankidService: BankIdRestService,
    private val signSessionRepository: SignSessionRepository,
    private val scheduler: Scheduler,
    private val memberService: MemberService,
    @Value("\${hedvig.bankid.signmessage.switcher}")
    private val switcherMessage: String,
    @Value("\${hedvig.bankid.signmessage.nonSwitcher}")
    private val nonSwitcherMessage: String,
    private val swedishBankIdMetrics: SwedishBankIdMetrics
) {

    fun startSign(request: WebsignRequest, memberId: Long, isSwitching: Boolean): MemberSignResponse {
        val response = startSign(memberId, request.ssn, request.ipAddress, isSwitching)

        return MemberSignResponse(
            signId = response.signId,
            status = SignStatus.IN_PROGRESS,
            bankIdOrderResponse = response.bankIdOrderResponse)
    }

    fun startSign(memberId: Long, ssn: String, ipAddress: String, isSwitching: Boolean): StartSwedishSignResponse {
        val session = signSessionRepository.findByMemberId(memberId).orElseGet { SignSession(memberId) }

        if (!session.canReuseBankIdSession()) {
            val result = bankidService.startSign(
                ssn,
                createUserSignText(isSwitching),
                ipAddress)
            session.newOrderStarted(result)
            signSessionRepository.save(session)
            scheduleCollectJob(result)

            swedishBankIdMetrics.startBankIdSign()

            return StartSwedishSignResponse(
                signId = session.sessionId,
                bankIdOrderResponse = result)
        }
        return StartSwedishSignResponse(
            signId = session.sessionId,
            bankIdOrderResponse = session.orderResponse)
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
    fun collectBankId(orderReference: String): Boolean {
        val session = signSessionRepository.findByOrderReference(orderReference)
        return session
            .map { s: SignSession ->
                if (s.status == SignStatus.IN_PROGRESS) {
                    try {
                        val response = bankidService.collect(orderReference)
                        val collectResponse = CollectResponse(response.status, response.hintCode)
                        s.newCollectResponse(collectResponse)
                        if (response.status == CollectStatus.complete) {
                            swedishBankIdMetrics.completeBankIdSign()
                            memberService.bankIdSignComplete(s.memberId, response)
                            s.status = SignStatus.COMPLETED
                        } else if (response.status == CollectStatus.failed) {
                            swedishBankIdMetrics.failBankIdSign(response.hintCode)
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
                logger.error("Could not find SignSession with orderReference: ", orderReference)
                false
            }
    }

    fun getSignSession(@NonNull memberId: Long): Optional<SignSession> {
        return signSessionRepository.findByMemberId(memberId)
    }

    @Transactional
    fun completeSession(id: String?) {
        val session = signSessionRepository.findByOrderReference(id)
        if (session.isPresent) {
            val s = session.get()
            s.status = SignStatus.COMPLETED
            signSessionRepository.save(s)
        }
    }

    fun notifyContractsCreated(memberId: Long) {
        val session = signSessionRepository.findByMemberId(memberId)
        if (session.isPresent) {
            val s = session.get()
            s.hasContract = true
            signSessionRepository.save(s)
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
}
