package com.hedvig.memberservice.web.dto

data class GenericBankIdAuthenticationRequest(
    val memberId: String,
    val personalNumber: String? = null
)
