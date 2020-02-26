package com.hedvig.external.event

import com.hedvig.external.authentication.dto.NorwegianAuthenticationResult
import org.springframework.context.ApplicationEvent

class NorwegianAuthenticationEvent(source: Any, val message: NorwegianAuthenticationResult) : ApplicationEvent(source)
