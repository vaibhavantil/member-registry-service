package com.hedvig.memberservice.listeners

import com.hedvig.external.event.NorwegianAuthenticationEvent
import com.hedvig.memberservice.services.redispublisher.RedisEventPublisher
import org.springframework.context.event.EventListener
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component

@Component
class AnnotationDrivenContextStartedListener(
    redisEventPublisher: RedisEventPublisher
) {

    @Async
    @EventListener
    fun handleNorwegianAuthEvent(norwegianAuthEvent: NorwegianAuthenticationEvent) {

    }
}
