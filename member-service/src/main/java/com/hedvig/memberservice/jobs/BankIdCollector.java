package com.hedvig.memberservice.jobs;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.QuartzJobBean;

public class BankIdCollector extends QuartzJobBean {

  static final Logger log = LoggerFactory.getLogger(BankIdCollector.class);

  @Autowired
  BankIdCollectorBean bankIdCollectorBean;

  @Override
  protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
    try {

      bankIdCollectorBean.execute(context);

    }catch (Exception e) {
      throw new JobExecutionException("Exception in job execution", e, true);
    }
  }
}