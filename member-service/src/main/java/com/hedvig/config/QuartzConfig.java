package com.hedvig.config;

import javax.annotation.PostConstruct;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

@Configuration
public class QuartzConfig {

  private static Logger log = LoggerFactory.getLogger(QuartzConfig.class);

  @Autowired
  Scheduler scheduler;

  @PostConstruct
  public void removeOldJobs() {

    try {

      if (scheduler.deleteJob(new JobKey("echo3", "test"))) {
        log.info("Quartz job eco3 was deleted");
      }

    } catch (SchedulerException ex) {
      log.error("Could not delete old QuartzJobs");
    }
  }
}
