package com.hedvig.memberservice.listeners

import com.hedvig.external.authentication.dto.NorwegianAuthenticationResult
import com.hedvig.external.event.NorwegianAuthenticationEvent
import com.hedvig.memberservice.services.redispublisher.AuthSessionUpdatedEventStatus
import com.hedvig.memberservice.services.redispublisher.RedisEventPublisher
import org.springframework.context.event.EventListener
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component

@Component
class NorwegianAuthEventListener(
    private val redisEventPublisher: RedisEventPublisher
) {

    @Async
    @EventListener
    fun handleNorwegianAuthEvent(norwegianAuthEvent: NorwegianAuthenticationEvent) {
        when (val result = norwegianAuthEvent.message) {
            is NorwegianAuthenticationResult.Completed ->
                redisEventPublisher.onAuthSessionUpdated(result.memberId, AuthSessionUpdatedEventStatus.SUCCESS)
            is NorwegianAuthenticationResult.Failed ->
                redisEventPublisher.onAuthSessionUpdated(result.memberId, AuthSessionUpdatedEventStatus.FAILED)
        }
    }
}


