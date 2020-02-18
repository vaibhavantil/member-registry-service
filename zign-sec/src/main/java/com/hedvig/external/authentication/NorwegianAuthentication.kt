package com.hedvig.external.authentication

import com.hedvig.external.authentication.dto.NorwegianAuthenticationCollectResponse
import com.hedvig.external.authentication.dto.NorwegianBankIdAuthenticationRequest
import com.hedvig.external.authentication.dto.NorwegianAuthenticationResponse
import java.util.*

interface NorwegianAuthentication {

    fun auth(request: NorwegianBankIdAuthenticationRequest): NorwegianAuthenticationResponse
    fun sign(request: NorwegianBankIdAuthenticationRequest): NorwegianAuthenticationResponse
    fun collect(sessionId: UUID): NorwegianAuthenticationCollectResponse
}

