package com.hedvig.external.authentication.dto

import com.hedvig.external.zignSec.repository.entitys.Identity
import java.util.*

sealed class ZignSecAuthenticationResult {
    data class Completed(
        val identity: Identity,
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

