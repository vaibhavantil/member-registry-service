package com.hedvig.external.authentication

import com.hedvig.external.authentication.dto.NorwegianAuthenticationRequest
import com.hedvig.external.authentication.dto.NorwegianAuthenticationResponse

interface NorwegianAuthentication {

    fun auth(request: NorwegianAuthenticationRequest): NorwegianAuthenticationResponse
}

