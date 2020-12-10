package com.hedvig.memberservice.services.signing.sweden.dto

import com.hedvig.memberservice.web.dto.UnderwriterStartSignSessionResponse

data class StartSwedishBankIdSignResponse(
    val autoStartToken: String?,
    val internalErrorMessage: String? = null
)

fun StartSwedishBankIdSignResponse.toUnderwriterStartSignSessionResponse() = UnderwriterStartSignSessionResponse.SwedishBankId(
    autoStartToken,
    internalErrorMessage
)
