package com.hedvig.external.authentication.dto

import java.util.*

sealed class NorwegianSignResult {
    data class Signed(
        val id: UUID,
        val memberId: Long,
        val ssn: String
    ) : NorwegianSignResult()

    data class Failed(
        val id: UUID,
        val memberId: Long
    ) : NorwegianSignResult()
}

