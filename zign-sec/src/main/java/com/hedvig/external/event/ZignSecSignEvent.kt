package com.hedvig.external.event

import com.hedvig.external.authentication.dto.ZignSecSignResult
import org.springframework.context.ApplicationEvent

class ZignSecSignEvent(source: Any, val message: ZignSecSignResult) : ApplicationEvent(source)
