package com.hedvig.external.event

import com.hedvig.external.authentication.dto.NorwegianAuthenticationCollectResponse
import org.springframework.context.ApplicationEvent

class NorwegianAuthenticationEvent(source: Any, val message: NorwegianAuthenticationCollectResponse) : ApplicationEvent(source)
