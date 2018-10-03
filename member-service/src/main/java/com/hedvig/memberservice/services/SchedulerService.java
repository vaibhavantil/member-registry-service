package com.hedvig.memberservice.services;

import org.quartz.Scheduler;
import org.quartz.SchedulerException;
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
  }
}
