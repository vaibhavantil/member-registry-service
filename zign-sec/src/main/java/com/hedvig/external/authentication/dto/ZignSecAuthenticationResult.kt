package com.hedvig.external.authentication.dto

import com.hedvig.external.zignSec.client.dto.ZignSecIdentity
import java.util.*

sealed class ZignSecAuthenticationResult {
    data class Completed(
        val identity: ZignSecIdentity,
        val id: UUID,
        val memberId: Long,
        val ssn: String,
        val authenticationMethod: ZignSecAuthenticationMethod
    ) : ZignSecAuthenticationResult()

    data class Failed(
        val id: UUID,
        val memberId: Long
    ) : ZignSecAuthenticationResult()
}

