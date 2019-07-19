package com.hedvig.memberservice.jobs

import com.hedvig.memberservice.query.CollectRepository
import com.hedvig.memberservice.services.v2.BankIdService
import org.quartz.JobExecutionContext
import javax.transaction.Transactional

@Transactional
class BankIdAuthCollectorBean(
    private val bankIdService: BankIdService,
    private val collectRepository: CollectRepository
) {

    fun execute(context: JobExecutionContext) {
        val referenceId = context.jobDetail.key.name

        val memberId = collectRepository
            .findById(referenceId)
            .orElse(null)
            ?.memberId ?: throw IllegalStateException("No member found for this collect job")

        bankIdService.authCollect(referenceId, memberId)
    }
}
