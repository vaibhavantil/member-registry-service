package com.hedvig.memberservice.services.apigateway

data class ReassignMemberDto(
    val oldMemberId: String,
    val newMemberId: String
)
