package com.hedvig.external.authentication.dto

import java.util.*

sealed class ZignSecAuthenticationResult {
    data class Completed(
        val id: UUID,
        val memberId: Long,
        val ssn: String,
        val authenticationMethod: ZignSecAuthenticationMethod,
        val firstName: String?,
        val lastName: String?
    ) : ZignSecAuthenticationResult()

    data class Failed(
        val id: UUID,
        val memberId: Long
    ) : ZignSecAuthenticationResult()
}

