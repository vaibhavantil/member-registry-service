package com.hedvig.memberservice.listeners

import com.hedvig.external.event.NorwegianSignEvent
import org.springframework.context.event.EventListener
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component

@Component
class NorwegianSignEventListener {

    @Async
    @EventListener
    fun handleNorwegianSignEvent(norwegianSignEvent: NorwegianSignEvent) {
        //TODO: member is sign publish to redis!
    }
}
