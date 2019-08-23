package com.hedvig.memberservice.entities;

import com.hedvig.memberservice.util.BeanUtil;
import com.hedvig.memberservice.services.events.SignSessionCompleteEvent;
import javax.persistence.PostUpdate;
import lombok.val;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SignSessionEntityListener {

  private Logger logger = LoggerFactory.getLogger(SignSessionEntityListener.class);

  @PostUpdate
  public void postUpdateSignSession(SignSession signSession) {
    logger.info("Triggering event after update signSession {} with status", signSession,
        signSession.status.name());
    val publisher = BeanUtil.getBean();
    publisher.publishEvent(new
        SignSessionCompleteEvent(signSession.getMemberId()));
  }

}
