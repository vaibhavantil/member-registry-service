package com.hedvig.memberservice.jobs

import com.hedvig.integration.productsPricing.ContractsService
import com.hedvig.integration.underwriter.dtos.SignMethod
import com.hedvig.memberservice.services.signing.simple.SimpleSigningService
import com.hedvig.memberservice.services.signing.zignsec.ZignSecSigningService
import com.hedvig.memberservice.services.signing.sweden.SwedishBankIdSigningService
import com.hedvig.memberservice.services.redispublisher.RedisEventPublisher
import com.hedvig.memberservice.util.logger
import org.quartz.JobExecutionContext
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import javax.transaction.Transactional

@Transactional
@Component
class ContractsCreatedCollectorBean(
    private val contractsService: ContractsService,
    private val swedishSigningService: SwedishBankIdSigningService,
    private val zignSecSigningService: ZignSecSigningService,
    private val simpleSigningService: SimpleSigningService,
    private val redisEventPublisher: RedisEventPublisher
) {

    fun execute(context: JobExecutionContext) {

        val memberId = context.mergedJobDataMap.getLongFromString("memberId")
        logger.info("Executing has contracts Collect job for memberId: $memberId")

        val stopCollecting = contractsService.hasContract(memberId)

        if (stopCollecting) {
            when (SignMethod.valueOf(context.mergedJobDataMap.getString("signMethod"))) {
                SignMethod.SWEDISH_BANK_ID -> swedishSigningService.notifyContractsCreated(memberId)
                SignMethod.NORWEGIAN_BANK_ID,
                SignMethod.DANISH_BANK_ID -> zignSecSigningService.notifyContractsCreated(memberId)
                SignMethod.SIMPLE_SIGN -> simpleSigningService.notifyContractsCreated(memberId)
            }
            redisEventPublisher.onSignSessionUpdate(memberId)

            context.scheduler.deleteJob(context.jobDetail.key)
        }
    }
}
