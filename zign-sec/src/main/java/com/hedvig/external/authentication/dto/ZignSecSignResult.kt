package com.hedvig.external.authentication.dto

import java.util.*

sealed class ZignSecSignResult {
    data class Signed(
        val id: UUID,
        val memberId: Long,
        val ssn: String,
        val providerJsonResponse: String,
        val authenticationMethod: ZignSecAuthenticationMethod
    ) : ZignSecSignResult()

    data class Failed(
        val id: UUID,
        val memberId: Long,
        val authenticationMethod: ZignSecAuthenticationMethod
    ) : ZignSecSignResult()
}

