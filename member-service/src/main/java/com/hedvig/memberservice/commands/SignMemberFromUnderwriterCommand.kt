package com.hedvig.memberservice.commands

import org.axonframework.commandhandling.TargetAggregateIdentifier

data class SignMemberFromUnderwriterCommand(
    @TargetAggregateIdentifier
    val id: Long,
    val ssn: String
)
