package com.hedvig.external.event

import com.hedvig.external.authentication.dto.ZignSecAuthenticationResult
import com.hedvig.external.authentication.dto.ZignSecSignResult
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Component

@Component
class AuthenticationEventPublisherImpl(
    private val applicationEventPublisher: ApplicationEventPublisher
): AuthenticationEventPublisher {

    override fun publishAuthenticationEvent(zignSecAuthResult: ZignSecAuthenticationResult) {
        val event = ZignSecAuthenticationEvent(this, zignSecAuthResult)
        applicationEventPublisher.publishEvent(event)
    }

    override fun publishSignEvent(zignSecSignResult: ZignSecSignResult) {
        val event = ZignSecSignEvent(this, zignSecSignResult)
        applicationEventPublisher.publishEvent(event)
    }
}
