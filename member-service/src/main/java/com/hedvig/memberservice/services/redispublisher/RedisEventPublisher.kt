package com.hedvig.memberservice.services.redispublisher

import com.hedvig.memberservice.services.events.AuthSessionCompleteEvent
import com.hedvig.memberservice.services.events.SignSessionCompleteEvent
import lombok.`val`
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener


@Component
class RedisEventPublisher(
    private val redisTemplate: RedisTemplate<String, Any>
) {

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun onSignSessionComplete(e: SignSessionCompleteEvent) {
        val message = SignSessionUpdatedEvent(SignSessionUpdatedEventStatus.UPDATED)
        redisTemplate.convertAndSend("SIGN_EVENTS.${e.memberId}", SignEvent(message))
    }

    fun onAuthSessionComplete(e: AuthSessionCompleteEvent) {
        val message = AuthSessionUpdatedEvent(AuthSessionUpdatedEventStatus.UPDATED)
        redisTemplate.convertAndSend("AUTH_EVENTS.${e.memberId}", AuthEvent(message))
    }
}


