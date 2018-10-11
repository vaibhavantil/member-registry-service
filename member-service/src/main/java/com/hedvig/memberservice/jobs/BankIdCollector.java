package com.hedvig.memberservice.jobs;

import com.hedvig.memberservice.services.SigningService;
import lombok.val;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.QuartzJobBean;

@DisallowConcurrentExecution
public class BankIdCollector extends QuartzJobBean {

  static final Logger log = LoggerFactory.getLogger(BankIdCollector.class);

  @Autowired
  SigningService signingService;

  @Override
  protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
    try {
      val referenceId = context.getJobDetail().getKey().getName();
      log.info("Starting BankIdCollectorJob for {}", referenceId);

      val bankIdSessionOpen = signingService.collectBankId(referenceId);

      if (bankIdSessionOpen == false) {
        log.info("Removing job ({}) from scheduler", referenceId);
        context.getScheduler().deleteJob(context.getJobDetail().getKey());
      }

    }catch (Exception e) {
      throw new JobExecutionException("Exception in job execution", e, true);
    }
  }
}