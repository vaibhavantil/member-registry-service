package com.hedvig.integration.underwritter.dtos

data class SignRequest(
    val referenceToken: String,
    val signature: String,
    val oscpResponse: String
)
