package com.hedvig.memberservice.jobs;

import com.hedvig.memberservice.services.SigningService;
import lombok.val;
import org.quartz.JobExecutionContext;
import org.quartz.SchedulerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * Spring wrapped bean that implements BankIdCollectorLogic
 */
@Transactional
@Component
public class BankIdCollectorBean {

  private static final Logger log = LoggerFactory.getLogger(BankIdCollectorBean.class);
  private final SigningService signingService;

  @Autowired
  public BankIdCollectorBean(SigningService signingService){
    this.signingService = signingService;
  }

  public  void execute(JobExecutionContext context) throws SchedulerException {
    TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
      @Override
      public void afterCompletion(int status) {
        log.info("BankIdCollectorBean transaction completed with status: {}", status);
      }
    });
    val referenceId = context.getJobDetail().getKey().getName();
    log.info("Starting BankIdCollectorJob for {}", referenceId);

    val bankIdSessionOpen = signingService.collectBankId(referenceId);

    if (bankIdSessionOpen == false) {
      log.info("Removing job ({}) from scheduler", referenceId);
      context.getScheduler().deleteJob(context.getJobDetail().getKey());
    }

  }

}
