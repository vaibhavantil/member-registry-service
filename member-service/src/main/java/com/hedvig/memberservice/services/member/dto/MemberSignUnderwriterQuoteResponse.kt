package com.hedvig.memberservice.services.member.dto

data class MemberSignUnderwriterQuoteResponse(
    val signId: Long,
    val memberIsSigned: Boolean
)
