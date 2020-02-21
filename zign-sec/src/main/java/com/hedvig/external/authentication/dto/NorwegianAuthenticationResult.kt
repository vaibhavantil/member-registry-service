package com.hedvig.external.authentication.dto

import java.util.*

sealed class NorwegianAuthenticationResult {
    data class Completed(
        val id: UUID,
        val ssn: String
    ) : NorwegianAuthenticationResult()

    data class Failed(
        val id: UUID
    ) : NorwegianAuthenticationResult()
}

