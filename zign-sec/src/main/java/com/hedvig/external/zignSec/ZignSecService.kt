package com.hedvig.external.zignSec

import com.hedvig.external.authentication.dto.NorwegianBankIdAuthenticationRequest
import com.hedvig.external.zignSec.client.dto.ZignSecCollectResponse
import com.hedvig.external.zignSec.client.dto.ZignSecResponse
import java.util.*

interface ZignSecService {
    fun auth(request: NorwegianBankIdAuthenticationRequest): ZignSecResponse
    fun collect(referenceId: UUID): ZignSecCollectResponse
}

