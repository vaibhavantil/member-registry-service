package com.hedvig.external.event

import com.hedvig.external.authentication.dto.NorwegianSignResult
import org.springframework.context.ApplicationEvent

class NorwegianSignEvent(source: Any, val message: NorwegianSignResult) : ApplicationEvent(source)
