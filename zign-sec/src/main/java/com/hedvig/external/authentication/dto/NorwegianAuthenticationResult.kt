package com.hedvig.external.authentication.dto

import java.util.*

sealed class NorwegianAuthenticationResult {
    data class Completed(
        val id: UUID,
        val memberId: Long,
        val ssn: String
    ) : NorwegianAuthenticationResult()

    data class Failed(
        val id: UUID,
        val memberId: Long
    ) : NorwegianAuthenticationResult()
}

