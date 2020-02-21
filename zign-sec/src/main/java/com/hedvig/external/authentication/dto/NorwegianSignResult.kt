package com.hedvig.external.authentication.dto

import java.util.*

sealed class NorwegianSignResult {
    data class Signed(
        val id: UUID,
        val ssn: String
    ) : NorwegianSignResult()

    data class Failed(
        val id: UUID
    ) : NorwegianSignResult()
}

