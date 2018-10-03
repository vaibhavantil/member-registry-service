package com.hedvig.memberservice.jobs;

import com.hedvig.external.bankID.bankIdRest.BankIdRestApi;
import com.hedvig.external.bankID.bankIdRestTypes.CollectRequest;
import lombok.val;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.quartz.QuartzJobBean;

public class BankIdCollector extends QuartzJobBean {

  static final Logger log = LoggerFactory.getLogger(EchoJob.class);
  BankIdRestApi api;

  @Override
  protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
    try {
      val referenceId = context.getJobDetail().getJobDataMap().getString("referenceId");

      val collectResponse = api.collect(new CollectRequest(referenceId));

      context.getScheduler().deleteJob(context.getJobDetail().getKey());


    }catch (Exception e) {
      throw new JobExecutionException("Exception in job execution", e, true);
    }
  }
}