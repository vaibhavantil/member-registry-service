package com.hedvig.external.authentication

import com.hedvig.external.authentication.dto.ZignSecBankIdAuthenticationRequest
import com.hedvig.external.authentication.dto.ZignSecBankIdProgressStatus
import com.hedvig.external.authentication.dto.StartZignSecAuthenticationResult

interface ZignSecAuthentication {

    fun auth(request: ZignSecBankIdAuthenticationRequest): StartZignSecAuthenticationResult
    fun sign(request: ZignSecBankIdAuthenticationRequest): StartZignSecAuthenticationResult
    fun getStatus(memberId: Long): ZignSecBankIdProgressStatus?
    fun notifyContractsCreated(memberId: Long)
}

