package com.hedvig.memberservice.services.dto

data class StartSwedishBankIdSignResponse(
    val autoStartToken: String?,
    val internalErrorMessage: String? = null
)
