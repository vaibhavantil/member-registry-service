package com.hedvig.memberservice.web.dto

data class RedirectBankIdAuthenticationRequest(
    val personalNumber: String? = null,
    val targetUrl: String,
    val failedTargetUrl: String
)
