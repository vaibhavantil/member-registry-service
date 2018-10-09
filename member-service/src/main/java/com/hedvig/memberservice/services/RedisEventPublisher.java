package com.hedvig.memberservice.services;

import com.hedvig.memberservice.services.events.SignSessionCompleteEvent;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;


@Component
public class RedisEventPublisher {

  private final RedisTemplate<String, Object> redisTemplate;

  public RedisEventPublisher(
      RedisTemplate<String, Object> redisTemplate) {
    this.redisTemplate = redisTemplate;
  }

  @TransactionalEventListener()
  public void onSignSessionComplete(SignSessionCompleteEvent e) {
    redisTemplate.convertAndSend(String.format("%s.%s", "SIGN_EVENTS", e.getMemberId()), new SignSessionUpdatedEvent(SignSessionUpdatedEventStatus.UPDATED));
  }

}

