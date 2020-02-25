package com.hedvig.integration.underwriter.dtos

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.util.*

@JsonIgnoreProperties(ignoreUnknown = true)
data class QuoteDto(
    val id: UUID,
    val currentInsurer: String? = null,
    val state: QuoteState,
    val signMethod: SignMethod
)
