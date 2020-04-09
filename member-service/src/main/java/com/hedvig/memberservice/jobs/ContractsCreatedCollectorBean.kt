package com.hedvig.memberservice.jobs

import com.hedvig.integration.productsPricing.ContractsService
import com.hedvig.integration.productsPricing.ProductApi
import com.hedvig.integration.underwriter.dtos.SignMethod
import com.hedvig.memberservice.query.CollectRepository
import com.hedvig.memberservice.services.NorwegianSigningService
import com.hedvig.memberservice.services.SwedishBankIdSigningService
import com.hedvig.memberservice.services.redispublisher.RedisEventPublisher
import com.hedvig.memberservice.services.v2.BankIdServiceV2
import org.quartz.JobExecutionContext
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import javax.transaction.Transactional

@Transactional
@Component
class ContractsCreatedCollectorBean(
    private val contractsService: ContractsService,
    private val swedishSigningService: SwedishBankIdSigningService,
    private val norwegianSigningService: NorwegianSigningService,
    private val redisEventPublisher: RedisEventPublisher
) {

    private val logger = LoggerFactory.getLogger(this::class.java)

    fun execute(context: JobExecutionContext) {

        val memberId = context.mergedJobDataMap.getLongFromString("memberId")
        logger.info("Executing has contracts Collect job for memberId: $memberId")

        val stopCollecting = contractsService.hasContract(memberId)

        if (stopCollecting) {
            when (SignMethod.valueOf(context.mergedJobDataMap.getString("signMethod"))) {
                SignMethod.SWEDISH_BANK_ID -> swedishSigningService.notifyContractsCreated(memberId)
                SignMethod.NORWEGIAN_BANK_ID -> norwegianSigningService.notifyContractsCreated(memberId)
            }
            redisEventPublisher.onSignSessionUpdate(memberId)

            context.scheduler.deleteJob(context.jobDetail.key)
        }
    }
}
