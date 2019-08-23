package com.hedvig.integration.apigateway

data class ReassignMemberDto(
    val oldMemberId: String,
    val newMemberId: String
)
