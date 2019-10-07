package com.hedvig.memberservice.services.member.dto

import com.hedvig.memberservice.entities.SignStatus

data class MemberSignUnderwriterQuoteResponse(
    val signId: Long,
    val memberIsSigned: Boolean
)
