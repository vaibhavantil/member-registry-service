package com.hedvig.memberservice.jobs

import org.quartz.JobExecutionContext
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.quartz.QuartzJobBean

class BankIdAuthCollector: QuartzJobBean() {

    @Autowired
    lateinit var bankIdAuthCollectorBean: BankIdAuthCollectorBean

    override fun executeInternal(context: JobExecutionContext) {
        bankIdAuthCollectorBean.execute(context)
    }
}
