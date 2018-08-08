package com.hedvig.memberservice.config;

import static org.quartz.SimpleScheduleBuilder.simpleSchedule;
import static org.quartz.TriggerBuilder.newTrigger;

import com.hedvig.memberservice.jobs.EchoJob;
import org.quartz.JobDetail;
import org.quartz.Trigger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.JobDetailFactoryBean;

@Configuration
public class QuartzConfig {

  Logger log = LoggerFactory.getLogger(QuartzConfig.class);

  @Bean(name="echo2")
  JobDetailFactoryBean echoJob() {
    JobDetailFactoryBean jobDetailFactoryBean = new JobDetailFactoryBean();
    jobDetailFactoryBean.setJobClass(EchoJob.class);
    jobDetailFactoryBean.setDurability(true);

    return jobDetailFactoryBean;
  }


  @Bean
  Trigger echoTrigger(JobDetail echoJob) {
    int frequencyInSec = 10;
    log.info("Configuring trigger");

    return newTrigger().withIdentity("echoTrigger").forJob(echoJob).withSchedule(simpleSchedule().withIntervalInSeconds(frequencyInSec).repeatForever()).build();

  }
}
