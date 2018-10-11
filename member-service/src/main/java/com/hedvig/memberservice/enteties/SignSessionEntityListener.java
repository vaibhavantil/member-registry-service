package com.hedvig.memberservice.enteties;

import com.hedvig.memberservice.BeanUtil;
import com.hedvig.memberservice.services.SignSessionUpdatedEvent;
import com.hedvig.memberservice.services.SignSessionUpdatedEventStatus;
import javax.persistence.PostUpdate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;

public class SignSessionEntityListener {

  private Logger logger = LoggerFactory.getLogger(SignSessionEntityListener.class);
  private ApplicationEventPublisher publisher = BeanUtil.getBean(ApplicationEventPublisher.class);

  @PostUpdate
  public void postUpdateSignSession(SignSession signSession) {
    logger.info("Triggering event after update signSession {} with status", signSession,
        signSession.status.name());

    publisher.publishEvent(new SignSessionUpdatedEvent(SignSessionUpdatedEventStatus.UPDATED));
  }

}