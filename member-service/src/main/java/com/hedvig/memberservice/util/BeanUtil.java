package com.hedvig.memberservice.util;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.stereotype.Service;

@Service
public class BeanUtil implements ApplicationEventPublisherAware {

  private static ApplicationEventPublisher applicationEventPublisher;


  public static  ApplicationEventPublisher getBean() {
    return applicationEventPublisher;
  }


  @Override
  public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {

    BeanUtil.applicationEventPublisher = applicationEventPublisher;
  }
}
