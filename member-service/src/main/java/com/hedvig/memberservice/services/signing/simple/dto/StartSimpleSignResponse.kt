package com.hedvig.memberservice.services.signing.simple.dto

data class StartSimpleSignResponse(
    val successfullyStarted: Boolean,
    val internalErrorMessage: String? = null
)
