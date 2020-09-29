package com.hedvig.external.event

import com.hedvig.external.authentication.dto.ZignSecAuthenticationResult
import org.springframework.context.ApplicationEvent

class ZignSecAuthenticationEvent(source: Any, val message: ZignSecAuthenticationResult) : ApplicationEvent(source)
