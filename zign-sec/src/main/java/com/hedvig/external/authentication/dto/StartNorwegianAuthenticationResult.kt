package com.hedvig.external.authentication.dto

import java.util.*

sealed class StartNorwegianAuthenticationResult {
    data class Success(
        val id: UUID,
        val redirectUrl: String
    ): StartNorwegianAuthenticationResult()

    data class Failed(
        val id: UUID,
        val errors: List<NorwegianAuthenticationResponseError>
    ): StartNorwegianAuthenticationResult()
}
