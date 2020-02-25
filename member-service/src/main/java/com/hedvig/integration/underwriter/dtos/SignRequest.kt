package com.hedvig.integration.underwriter.dtos

data class SignRequest(
    val referenceToken: String,
    val signature: String,
    val oscpResponse: String
)
