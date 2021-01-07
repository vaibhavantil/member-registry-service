package com.hedvig.memberservice.commands

import org.axonframework.commandhandling.TargetAggregateIdentifier

class UpdateEmailCommand(
    @TargetAggregateIdentifier
    val id: Long,
    val email: String
)
