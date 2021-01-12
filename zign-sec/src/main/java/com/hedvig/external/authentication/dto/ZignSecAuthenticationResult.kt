package com.hedvig.external.authentication.dto

import java.util.*

sealed class ZignSecAuthenticationResult {
    data class Completed(
        val id: UUID,
        val memberId: Long,
        val ssn: String,
        val providerJsonResponse: String,
        val authenticationMethod: ZignSecAuthenticationMethod
    ) : ZignSecAuthenticationResult()

    data class Failed(
        val id: UUID,
        val memberId: Long
    ) : ZignSecAuthenticationResult()
}

