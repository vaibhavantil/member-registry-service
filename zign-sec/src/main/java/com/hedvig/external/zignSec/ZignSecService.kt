package com.hedvig.external.zignSec

import com.hedvig.external.authentication.dto.ZignSecBankIdAuthenticationRequest
import com.hedvig.external.zignSec.client.dto.ZignSecCollectResponse
import com.hedvig.external.zignSec.client.dto.ZignSecResponse
import java.util.*

interface ZignSecService {
    fun auth(request: ZignSecBankIdAuthenticationRequest): ZignSecResponse
    fun collect(referenceId: UUID): ZignSecCollectResponse
}

