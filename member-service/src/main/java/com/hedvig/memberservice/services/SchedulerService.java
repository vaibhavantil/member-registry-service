package com.hedvig.memberservice.services;

import static org.quartz.JobBuilder.newJob;
import static org.quartz.SimpleScheduleBuilder.simpleSchedule;
import static org.quartz.TriggerBuilder.newTrigger;

import com.hedvig.memberservice.jobs.EchoJob;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.TriggerKey;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class SchedulerService {

  private final Scheduler scheduler;

  public SchedulerService(Scheduler scheduler) {
    this.scheduler = scheduler;
  }

  @EventListener(ApplicationReadyEvent.class)
  public void doSomethingAfterStartup() throws SchedulerException {

    final JobKey jobKey = JobKey.jobKey("echo3", "test");
    if (!scheduler.checkExists(jobKey)) {
      scheduler.addJob(newJob().ofType(EchoJob.class).storeDurably().withIdentity("echo3","test").build(),false);
    }

    final TriggerKey triggerKey = TriggerKey.triggerKey("echo3", "test");
    if(!scheduler.checkExists(triggerKey)){
      scheduler.scheduleJob(newTrigger().forJob(jobKey).withIdentity(triggerKey).startNow().withSchedule(simpleSchedule().withIntervalInSeconds(2).repeatForever()).build());
    }

    System.out.println("hello world, I have just started up");
  }
}
