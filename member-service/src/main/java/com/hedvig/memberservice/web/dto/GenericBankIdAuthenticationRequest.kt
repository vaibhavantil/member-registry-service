package com.hedvig.memberservice.web.dto

data class GenericBankIdAuthenticationRequest(
    val personalNumber: String?,
    val token: String
)
