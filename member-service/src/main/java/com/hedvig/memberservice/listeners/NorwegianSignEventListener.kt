package com.hedvig.memberservice.listeners

import com.hedvig.external.event.NorwegianSignEvent
import com.hedvig.memberservice.services.NorwegianSigningService
import org.springframework.context.event.EventListener
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component

@Component
class NorwegianSignEventListener(
    val norwegianSigningService: NorwegianSigningService
) {

    @Async
    @EventListener
    fun handleNorwegianSignEvent(norwegianSignEvent: NorwegianSignEvent) {
        norwegianSigningService.handleSignResult(norwegianSignEvent.message)
    }
}