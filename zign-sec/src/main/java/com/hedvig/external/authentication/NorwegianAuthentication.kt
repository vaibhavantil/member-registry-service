package com.hedvig.external.authentication

import com.hedvig.external.authentication.dto.NorwegianBankIdAuthenticationRequest
import com.hedvig.external.authentication.dto.StartNorwegianAuthenticationResult

interface NorwegianAuthentication {

    fun auth(request: NorwegianBankIdAuthenticationRequest): StartNorwegianAuthenticationResult
    fun sign(request: NorwegianBankIdAuthenticationRequest): StartNorwegianAuthenticationResult
}

