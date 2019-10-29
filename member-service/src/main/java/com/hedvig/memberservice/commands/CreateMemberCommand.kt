package com.hedvig.memberservice.commands

data class CreateMemberCommand(
    @TargetAggregateIdentifier
    val memberId: Long,
    val acceptLanguage: String? = null
)
