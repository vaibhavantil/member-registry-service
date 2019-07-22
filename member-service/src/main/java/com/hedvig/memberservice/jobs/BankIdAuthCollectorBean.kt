package com.hedvig.memberservice.jobs

import com.hedvig.memberservice.query.CollectRepository
import com.hedvig.memberservice.services.v2.BankIdServiceV2
import org.quartz.JobExecutionContext
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import javax.transaction.Transactional

@Transactional
@Component
class BankIdAuthCollectorBean(
    private val bankIdService: BankIdServiceV2,
    private val collectRepository: CollectRepository
) {

    private val logger = LoggerFactory.getLogger(this::class.java)

    fun execute(context: JobExecutionContext) {
        val referenceId = context.jobDetail.key.name

        logger.info("Executing Auth Collect job for referenceId: $referenceId")

        val memberId = collectRepository
            .findById(referenceId)
            .orElse(null)
            ?.memberId ?: throw IllegalStateException("No member found for this collect job")

        val stopCollecting = bankIdService.authCollect(referenceId, memberId)

        if (stopCollecting) {
            context.scheduler.deleteJob(context.jobDetail.key)
        }
    }
}
