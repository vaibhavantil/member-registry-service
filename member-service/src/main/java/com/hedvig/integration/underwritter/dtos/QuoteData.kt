package com.hedvig.integration.underwritter.dtos

import java.util.*

data class QuoteDto(
    val id: UUID,
    val currentInsurer: String? = null,
    val state: QuoteState
)

