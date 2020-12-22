package com.hedvig.memberservice.listeners

import com.hedvig.external.event.ZignSecAuthenticationEvent
import com.hedvig.memberservice.services.signing.zignsec.ZignSecBankIdService
import org.springframework.context.event.EventListener
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component

@Component
class NorwegianAuthEventListener(
    private val zignSecBankIdService: ZignSecBankIdService
) {

    @Async
    @EventListener
    fun handleNorwegianAuthEvent(zignSecAuthEvent: ZignSecAuthenticationEvent) {
        zignSecBankIdService.completeAuthentication(zignSecAuthEvent.message)
    }
}


