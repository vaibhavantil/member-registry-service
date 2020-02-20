package com.hedvig.external.event

import com.hedvig.external.authentication.dto.NorwegianAuthenticationCollectResponse
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Component

@Component
class NorwegianAuthenticationEventPublisherImpl(
    private val applicationEventPublisher: ApplicationEventPublisher
): NorwegianAuthenticationEventPublisher {
    override fun publishAuthenticationEvent(norwegianAuthenticationCollectResponse: NorwegianAuthenticationCollectResponse) {
        val event = NorwegianAuthenticationEvent(this, norwegianAuthenticationCollectResponse)
        applicationEventPublisher.publishEvent(event)
    }

    override fun publishSignEvent(norwegianAuthenticationCollectResponse: NorwegianAuthenticationCollectResponse) {
        val event = NorwegianSignEvent(this, norwegianAuthenticationCollectResponse)
        applicationEventPublisher.publishEvent(event)
    }
}
