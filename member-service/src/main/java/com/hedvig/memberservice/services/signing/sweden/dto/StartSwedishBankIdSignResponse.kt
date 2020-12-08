package com.hedvig.memberservice.services.signing.sweden.dto

data class StartSwedishBankIdSignResponse(
    val autoStartToken: String?,
    val internalErrorMessage: String? = null
)
