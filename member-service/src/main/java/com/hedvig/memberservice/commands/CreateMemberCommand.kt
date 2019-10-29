package com.hedvig.memberservice.commands

import org.axonframework.commandhandling.TargetAggregateIdentifier

data class CreateMemberCommand(
    @TargetAggregateIdentifier
    val memberId: Long,
    val acceptLanguage: String? = null
)
