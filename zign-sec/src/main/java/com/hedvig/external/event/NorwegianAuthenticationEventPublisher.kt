package com.hedvig.external.event

import com.hedvig.external.authentication.dto.NorwegianAuthenticationCollectResponse

interface NorwegianAuthenticationEventPublisher {

    fun publishAuthenticationEvent(norwegianAuthenticationCollectResponse: NorwegianAuthenticationCollectResponse)
    fun publishSignEvent(norwegianAuthenticationCollectResponse: NorwegianAuthenticationCollectResponse)
}
