package com.hedvig.external.authentication.dto

data class NorwegianBankIdAuthenticationRequest(
    val memberId: String,
    val personalNumber: String? = null,
    val language: String,
    val successUrl: String,
    val failUrl: String
)
