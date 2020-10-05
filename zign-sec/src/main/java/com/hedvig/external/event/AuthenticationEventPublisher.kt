package com.hedvig.external.event

import com.hedvig.external.authentication.dto.ZignSecAuthenticationResult
import com.hedvig.external.authentication.dto.ZignSecSignResult

interface AuthenticationEventPublisher {

    fun publishAuthenticationEvent(authResult: ZignSecAuthenticationResult)
    fun publishSignEvent(secSignResult: ZignSecSignResult)
}
