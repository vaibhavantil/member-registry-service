package com.hedvig.external.zignSec

import com.hedvig.external.authentication.dto.NorwegianBankIdAuthenticationRequest
import com.hedvig.external.zignSec.client.dto.ZignSecResponse

interface ZignSecService {
    fun auth(request: NorwegianBankIdAuthenticationRequest): ZignSecResponse
}

