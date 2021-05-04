package com.hedvig.memberservice.commands

import org.axonframework.commandhandling.TargetAggregateIdentifier

data class DeleteMemberCommand(
    @TargetAggregateIdentifier
    val memberId: Long
)
