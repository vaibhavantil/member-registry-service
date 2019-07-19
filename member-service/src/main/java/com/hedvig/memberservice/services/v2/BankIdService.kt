package com.hedvig.memberservice.services.v2

import com.hedvig.external.bankID.bankidTypes.OrderResponse
import com.hedvig.external.bankID.bankidTypes.ProgressStatus
import com.hedvig.memberservice.commands.InactivateMemberCommand
import com.hedvig.memberservice.jobs.BankIdAuthCollector
import com.hedvig.memberservice.query.CollectRepository
import com.hedvig.memberservice.query.CollectType
import com.hedvig.memberservice.query.SignedMemberRepository
import com.hedvig.memberservice.services.bankid.BankIdSOAPApi
import com.hedvig.memberservice.services.events.AuthSessionCompleteEvent
import com.hedvig.memberservice.services.redispublisher.RedisEventPublisher
import org.axonframework.commandhandling.gateway.CommandGateway
import org.quartz.JobBuilder
import org.quartz.Scheduler
import org.quartz.SimpleScheduleBuilder
import org.quartz.TriggerBuilder
import javax.transaction.Transactional

class BankIdService(
    private val bankIdSOAPApi: BankIdSOAPApi,
    private val signedMemberRepository: SignedMemberRepository,
    private val commandGateway: CommandGateway,
    private val redisEventPublisher: RedisEventPublisher,
    private val scheduler: Scheduler,
    private val collectRepository: CollectRepository

) {
    @Transactional
    fun auth(memberId: Long): OrderResponse {
        val status = bankIdSOAPApi.auth()

        trackAuthToken(status.orderRef, memberId)
        scheduleCollectJob(status.orderRef)

        return status
    }

    fun authCollect(referenceToken: String, memberId: Long) {
        val bankIdRes = bankIdSOAPApi.authCollect(referenceToken)

        when (bankIdRes.progressStatus) {
            ProgressStatus.OUTSTANDING_TRANSACTION -> {} // TODO: Emit events on Redis for these statuses
            ProgressStatus.NO_CLIENT -> {}
            ProgressStatus.STARTED -> {}
            ProgressStatus.USER_SIGN -> {}
            ProgressStatus.USER_REQ -> {}
            ProgressStatus.COMPLETE -> {
                val personalNumber = bankIdRes.userInfo.personalNumber
                val signedMember = signedMemberRepository.findBySsn(personalNumber)
                if (signedMember.isPresent) {
                    commandGateway.sendAndWait<Any>(InactivateMemberCommand(memberId))
                }
                redisEventPublisher.onAuthSessionComplete(AuthSessionCompleteEvent(memberId))
            }
            else -> {}
        }
    }

    private fun scheduleCollectJob(orderReference: String) {
        val jobDetail = JobBuilder
            .newJob()
            .withIdentity(orderReference, JOB_GROUP)
            .ofType(BankIdAuthCollector::class.java)
            .build()

        val trigger = TriggerBuilder
            .newTrigger()
            .forJob(orderReference, JOB_GROUP)
            .withSchedule(
                SimpleScheduleBuilder
                    .simpleSchedule()
                    .withIntervalInSeconds(1)
                    .withRepeatCount(900)
                    .withMisfireHandlingInstructionNowWithRemainingCount()
            )
            .build()

        scheduler.scheduleJob(jobDetail, trigger)
    }

    private fun trackAuthToken(referenceToken: String, memberId: Long?) {
        trackReferenceToken(referenceToken, CollectType.RequestType.AUTH, memberId)
    }

    private fun trackReferenceToken(referenceToken: String, sign: CollectType.RequestType, memberId: Long?) {
        val ct = CollectType()
        ct.token = referenceToken
        ct.type = sign
        ct.memberId = memberId
        collectRepository.save(ct)
    }

    companion object {
        const val JOB_GROUP = "bankid.auth.collect"
    }
}
