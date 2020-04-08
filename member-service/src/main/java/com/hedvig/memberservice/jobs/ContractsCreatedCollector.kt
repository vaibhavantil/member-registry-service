package com.hedvig.memberservice.jobs

import org.quartz.JobExecutionContext
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.quartz.QuartzJobBean

class ContractsCreatedCollector: QuartzJobBean() {

    @Autowired
    lateinit var contractsCreatedCollector: ContractsCreatedCollector

    override fun executeInternal(context: JobExecutionContext) {
        contractsCreatedCollector.execute(context)
    }
}
