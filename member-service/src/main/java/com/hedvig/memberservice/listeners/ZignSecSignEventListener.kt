package com.hedvig.memberservice.listeners

import com.hedvig.external.event.ZignSecSignEvent
import com.hedvig.memberservice.services.signing.zignsec.ZignSecSigningService
import org.springframework.context.event.EventListener
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component

@Component
class ZignSecSignEventListener(
    val zignSecSigningService: ZignSecSigningService
) {

    @Async
    @EventListener
    fun handleSignEvent(zignSecSignEvent: ZignSecSignEvent) {
        zignSecSigningService.handleSignResult(zignSecSignEvent.message)
    }
}
