package com.hedvig.memberservice.externalApi.apigateway

data class ReassignMemberDto(
    val oldMemberId: String,
    val newMemberId: String
)
