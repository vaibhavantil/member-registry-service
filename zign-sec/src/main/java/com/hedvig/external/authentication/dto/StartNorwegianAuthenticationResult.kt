package com.hedvig.external.authentication.dto

import java.util.*

sealed class StartNorwegianAuthenticationResult {
    data class Success(
        val sessionId: Long,
        val redirectUrl: String
    ): StartNorwegianAuthenticationResult()

    data class Failed(
        val errors: List<NorwegianAuthenticationResponseError>
    ): StartNorwegianAuthenticationResult()
}
