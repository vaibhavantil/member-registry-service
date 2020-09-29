package com.hedvig.external.authentication.dto

data class ZignSecBankIdAuthenticationRequest(
    val memberId: String,
    val personalNumber: String? = null,
    val language: String,
    val successUrl: String,
    val failUrl: String,
    val authMethod: ZignSecAuthenticationMethod
)
