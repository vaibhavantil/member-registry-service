package com.hedvig.memberservice.listeners

import com.hedvig.external.event.NorwegianAuthenticationEvent
import com.hedvig.memberservice.services.NorwegianBankIdService
import org.springframework.context.event.EventListener
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component

@Component
class NorwegianAuthEventListener(
    private val norwegianBankIdService: NorwegianBankIdService
) {

    @Async
    @EventListener
    fun handleNorwegianAuthEvent(norwegianAuthEvent: NorwegianAuthenticationEvent) {
        norwegianBankIdService.completeAuthentication(norwegianAuthEvent.message)
    }
}


