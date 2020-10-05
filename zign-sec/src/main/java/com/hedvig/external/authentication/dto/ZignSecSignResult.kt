package com.hedvig.external.authentication.dto

import java.util.*

sealed class ZignSecSignResult {

    abstract val id: UUID
    abstract val authenticationMethod: ZignSecAuthenticationMethod

    data class Signed(
        override val id: UUID,
        val memberId: Long,
        val ssn: String,
        val providerJsonResponse: String,
        override val authenticationMethod: ZignSecAuthenticationMethod
    ) : ZignSecSignResult()

    data class Failed(
        override val id: UUID,
        val memberId: Long,
        override val authenticationMethod: ZignSecAuthenticationMethod
    ) : ZignSecSignResult()
}

