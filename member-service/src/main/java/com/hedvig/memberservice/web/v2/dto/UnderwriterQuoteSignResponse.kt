package com.hedvig.memberservice.web.v2.dto

import com.hedvig.memberservice.entities.SignStatus

data class UnderwriterQuoteSignResponse(
    val signId: Long,
    val memberIsSigned: Boolean
)



