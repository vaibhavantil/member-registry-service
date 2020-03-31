package com.hedvig.memberservice.web.dto

data class RedirectBankIdAuthenticationRequest(
    val personalNumber: String? = null,
    val successUrl: String,
    val failUrl: String
)
