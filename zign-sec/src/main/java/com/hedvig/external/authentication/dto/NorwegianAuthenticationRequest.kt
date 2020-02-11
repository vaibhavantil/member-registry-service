package com.hedvig.external.authentication.dto

data class NorwegianAuthenticationRequest(
    val personalNumber: String,
    val language: String,
    val isMobile: Boolean
)
