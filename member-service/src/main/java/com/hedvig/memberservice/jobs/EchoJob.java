package com.hedvig.memberservice.jobs;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.quartz.QuartzJobBean;

public class EchoJob extends QuartzJobBean {

  Logger log = LoggerFactory.getLogger(EchoJob.class);

  @Override
  protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
    try {
      log.info("Yay im a scheduled job!");

    }catch (Exception e) {
      throw new JobExecutionException("Exception in job execution", e, true);
    }
  }
}
