package com.hedvig.external.authentication.dto

data class NorwegianBankIdAuthenticationRequest(
    val memberId: String,
    val personalNumber: String,
    val language: String,
    val isMobile: Boolean
)
