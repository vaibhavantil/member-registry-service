package com.hedvig.external.event

import com.hedvig.external.authentication.dto.NorwegianAuthenticationResult
import com.hedvig.external.authentication.dto.NorwegianSignResult

interface NorwegianAuthenticationEventPublisher {

    fun publishAuthenticationEvent(norwegianAuthResult: NorwegianAuthenticationResult)
    fun publishSignEvent(norwegianSignResult: NorwegianSignResult)
}
