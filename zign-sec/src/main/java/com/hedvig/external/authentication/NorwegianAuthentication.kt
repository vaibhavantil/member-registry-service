package com.hedvig.external.authentication

import com.hedvig.external.authentication.dto.NorwegianAuthenticationCollectResponse
import com.hedvig.external.authentication.dto.NorwegianBankIdAuthenticationRequest
import com.hedvig.external.authentication.dto.StartNorwegianAuthenticationResult
import java.util.*

interface NorwegianAuthentication {

    fun auth(request: NorwegianBankIdAuthenticationRequest): StartNorwegianAuthenticationResult
    fun sign(request: NorwegianBankIdAuthenticationRequest): StartNorwegianAuthenticationResult
    fun collect(sessionId: UUID): NorwegianAuthenticationCollectResponse
}

