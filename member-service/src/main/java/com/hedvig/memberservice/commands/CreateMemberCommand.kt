package com.hedvig.memberservice.commands

data class CreateMemberCommand(
    val memberId: Long,
    val acceptLanguage: String? = null
)
