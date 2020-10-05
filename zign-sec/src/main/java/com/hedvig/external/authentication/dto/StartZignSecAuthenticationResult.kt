package com.hedvig.external.authentication.dto

import java.util.*

sealed class StartZignSecAuthenticationResult {
    data class Success(
        val orderReference: UUID,
        val redirectUrl: String
    ): StartZignSecAuthenticationResult()

    data class Failed(
        val errors: List<ZignSecAuthenticationResponseError>
    ): StartZignSecAuthenticationResult()
}
