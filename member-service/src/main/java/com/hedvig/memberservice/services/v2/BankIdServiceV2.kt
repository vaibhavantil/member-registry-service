package com.hedvig.memberservice.services.v2

import com.hedvig.external.bankID.bankId.BankIdApi
import com.hedvig.external.bankID.bankIdTypes.CollectRequest
import com.hedvig.external.bankID.bankIdTypes.CollectStatus
import com.hedvig.external.bankID.bankIdTypes.OrderAuthRequest
import com.hedvig.external.bankID.bankIdTypes.OrderResponse
import com.hedvig.memberservice.commands.InactivateMemberCommand
import com.hedvig.memberservice.jobs.BankIdAuthCollector
import com.hedvig.memberservice.query.CollectRepository
import com.hedvig.memberservice.query.CollectType
import com.hedvig.memberservice.query.SignedMemberRepository
import com.hedvig.integration.apigateway.ApiGatewayService
import com.hedvig.memberservice.jobs.SwedishBankIdMetrics
import com.hedvig.memberservice.services.redispublisher.AuthSessionUpdatedEventStatus
import com.hedvig.memberservice.services.redispublisher.RedisEventPublisher
import com.hedvig.memberservice.util.logger
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
    private val bankIdApi: BankIdApi,
    private val signedMemberRepository: SignedMemberRepository,
    private val commandGateway: CommandGateway,
    private val redisEventPublisher: RedisEventPublisher,
    private val scheduler: Scheduler,
    private val collectRepository: CollectRepository,
    private val apiGatewayService: ApiGatewayService,
    private val swedishBankIdMetrics: SwedishBankIdMetrics

) {

    @Transactional
    fun auth(memberId: Long, endUserIp: String): OrderResponse {
        val status = bankIdApi.auth(OrderAuthRequest(endUserIp))

        trackAuthToken(status.orderRef, memberId)
        scheduleCollectJob(status.orderRef)
        swedishBankIdMetrics.startBankIdV2Auth()

        return status
    }

    fun authCollect(referenceToken: String, memberId: Long): Boolean {
        try {
            val bankIdRes = bankIdApi.collect(CollectRequest(referenceToken))
            when (bankIdRes.status) {
                CollectStatus.pending -> {
                    when (bankIdRes.hintCode) {
                        "outstandingTransaction", "noClient" -> {
                            redisEventPublisher.onAuthSessionUpdated(memberId, AuthSessionUpdatedEventStatus.INITIATED)
                        }
                        "started", "userSign" -> {
                            redisEventPublisher.onAuthSessionUpdated(memberId, AuthSessionUpdatedEventStatus.IN_PROGRESS)
                        }
                        else -> {
                            logger.error("Got unknown hint code on auth collect. Is pending. Hint code: ${bankIdRes.hintCode}")
                            redisEventPublisher.onAuthSessionUpdated(memberId, AuthSessionUpdatedEventStatus.UNKNOWN)
                        }
                    }
                }
                CollectStatus.failed -> {
                    swedishBankIdMetrics.failedBankIdV2Auth(bankIdRes.hintCode)
                    when (bankIdRes.hintCode) {
                        "userCancel", "cancelled"-> {
                            redisEventPublisher.onAuthSessionUpdated(memberId, AuthSessionUpdatedEventStatus.FAILED)
                        }
                        "expiredTransaction", "certificateErr", "startFailed" -> {
                            logger.error("BankId auth failed. With known error. Hint code: ${bankIdRes.hintCode}. Reference token: $referenceToken")
                            redisEventPublisher.onAuthSessionUpdated(memberId, AuthSessionUpdatedEventStatus.FAILED)
                        } else -> {
                            logger.error("Got unknown hint code on auth collect. Failed. Hint code: ${bankIdRes.hintCode}")
                            redisEventPublisher.onAuthSessionUpdated(memberId, AuthSessionUpdatedEventStatus.FAILED)
                        }
                    }
                }
                CollectStatus.complete -> {
                    swedishBankIdMetrics.completeBankIdV2Auth()
                    val personalNumber = bankIdRes.completionData.user.personalNumber
                    val signedMember = signedMemberRepository.findBySsn(personalNumber)
                    if (signedMember.isPresent) {
                        if (memberId != signedMember.get().id) {
                            commandGateway.sendAndWait<Any>(InactivateMemberCommand(memberId))
                            apiGatewayService.reassignMember(memberId, signedMember.get().id)
                        }
                        redisEventPublisher.onAuthSessionUpdated(memberId, AuthSessionUpdatedEventStatus.SUCCESS)
                    } else {
                        redisEventPublisher.onAuthSessionUpdated(memberId, AuthSessionUpdatedEventStatus.FAILED)
                    }
                    return true
                }
                else -> {}
            }
        } catch (e: Exception) {
            logger.error(e.toString(), e)
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
