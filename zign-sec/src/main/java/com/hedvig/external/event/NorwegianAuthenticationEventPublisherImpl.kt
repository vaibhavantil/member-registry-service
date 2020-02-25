package com.hedvig.external.event

import com.hedvig.external.authentication.dto.NorwegianAuthenticationResult
import com.hedvig.external.authentication.dto.NorwegianSignResult
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Component

@Component
class NorwegianAuthenticationEventPublisherImpl(
    private val applicationEventPublisher: ApplicationEventPublisher
): NorwegianAuthenticationEventPublisher {

    override fun publishAuthenticationEvent(norwegianAuthResult: NorwegianAuthenticationResult) {
        val event = NorwegianAuthenticationEvent(this, norwegianAuthResult)
        applicationEventPublisher.publishEvent(event)
    }

    override fun publishSignEvent(norwegianSignResult: NorwegianSignResult) {
        val event = NorwegianSignEvent(this, norwegianSignResult)
        applicationEventPublisher.publishEvent(event)
    }
}
