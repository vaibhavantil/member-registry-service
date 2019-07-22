package com.hedvig.memberservice.services.v2

import com.hedvig.external.bankID.bankidTypes.OrderResponse
import com.hedvig.external.bankID.bankidTypes.ProgressStatus
import com.hedvig.memberservice.commands.InactivateMemberCommand
import com.hedvig.memberservice.jobs.BankIdAuthCollector
import com.hedvig.memberservice.query.CollectRepository
import com.hedvig.memberservice.query.CollectType
import com.hedvig.memberservice.query.SignedMemberRepository
import com.hedvig.memberservice.services.apigateway.ApiGatewayService
import com.hedvig.memberservice.services.bankid.BankIdSOAPApi
import com.hedvig.memberservice.services.events.AuthSessionCompleteEvent
import com.hedvig.memberservice.services.redispublisher.AuthSessionUpdatedEventStatus
import com.hedvig.memberservice.services.redispublisher.RedisEventPublisher
import org.axonframework.commandhandling.gateway.CommandGateway
import org.quartz.JobBuilder
import org.quartz.Scheduler
import org.quartz.SimpleScheduleBuilder
import org.quartz.TriggerBuilder
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.lang.Exception
import javax.transaction.Transactional

@Service
class BankIdServiceV2(
    private val bankIdSOAPApi: BankIdSOAPApi,
    private val signedMemberRepository: SignedMemberRepository,
    private val commandGateway: CommandGateway,
    private val redisEventPublisher: RedisEventPublisher,
    private val scheduler: Scheduler,
    private val collectRepository: CollectRepository,
    private val apiGatewayService: ApiGatewayService

) {
    private val logger = LoggerFactory.getLogger(this::class.java)

    @Transactional
    fun auth(memberId: Long): OrderResponse {
        val status = bankIdSOAPApi.auth()

        trackAuthToken(status.orderRef, memberId)
        scheduleCollectJob(status.orderRef)

        return status
    }

    fun authCollect(referenceToken: String, memberId: Long): Boolean {
        try {
            val bankIdRes = bankIdSOAPApi.authCollect(referenceToken)
            when (bankIdRes.progressStatus) {
                ProgressStatus.OUTSTANDING_TRANSACTION, ProgressStatus.NO_CLIENT -> {
                    redisEventPublisher.onAuthSessionUpdated(memberId, AuthSessionUpdatedEventStatus.INITIATED)
                }
                ProgressStatus.STARTED, ProgressStatus.USER_SIGN -> {
                    redisEventPublisher.onAuthSessionUpdated(memberId, AuthSessionUpdatedEventStatus.IN_PROGRESS)
                }
                ProgressStatus.COMPLETE -> {
                    val personalNumber = bankIdRes.userInfo.personalNumber
                    val signedMember = signedMemberRepository.findBySsn(personalNumber)
                    if (signedMember.isPresent) {
                        commandGateway.sendAndWait<Any>(InactivateMemberCommand(memberId))
                        apiGatewayService.reassignMember(memberId, signedMember.get().id)
                        redisEventPublisher.onAuthSessionUpdated(memberId, AuthSessionUpdatedEventStatus.SUCCESS)
                    } else {
                        redisEventPublisher.onAuthSessionUpdated(memberId, AuthSessionUpdatedEventStatus.FAILED)
                    }
                    return true
                }
                else -> {}
            }
        } catch (e: Exception) {
            logger.error(e.toString())
            redisEventPublisher.onAuthSessionUpdated(memberId, AuthSessionUpdatedEventStatus.FAILED)
            return true
        }

        return false
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


    private fun trackAuthToken(referenceToken: String, memberId: Long) {
        val ct = CollectType()
        ct.token = referenceToken
        ct.type = CollectType.RequestType.AUTH
        ct.memberId = memberId
        collectRepository.save(ct)
    }

    companion object {
        const val JOB_GROUP = "bankid.auth.collect"
    }
}
