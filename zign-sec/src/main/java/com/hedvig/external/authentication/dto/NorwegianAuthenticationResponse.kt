package com.hedvig.external.authentication.dto

import java.util.*

data class NorwegianAuthenticationResponse(
    val id: UUID,
    val errors: List<NorwegianAuthenticationResponseError>
)
