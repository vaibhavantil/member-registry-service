package com.hedvig.memberservice.services.redispublisher;

import com.hedvig.memberservice.services.events.SignSessionCompleteEvent;
import lombok.val;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;


@Component
public class RedisEventPublisher {

  private final RedisTemplate<String, Object> redisTemplate;

  public RedisEventPublisher(
      RedisTemplate<String, Object> redisTemplate) {
    this.redisTemplate = redisTemplate;
  }

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void onSignSessionComplete(SignSessionCompleteEvent e) {
    val message = new SignSessionUpdatedEvent(
        SignSessionUpdatedEventStatus.UPDATED);
    redisTemplate.convertAndSend(String.format("%s.%s", "SIGN_EVENTS", e.getMemberId()),
        new SignEvent(message));
  }

}

