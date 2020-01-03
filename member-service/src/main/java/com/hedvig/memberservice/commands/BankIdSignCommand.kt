package com.hedvig.memberservice.commands

import org.axonframework.commandhandling.TargetAggregateIdentifier

data class BankIdSignCommand(
    @TargetAggregateIdentifier val id: Long,
    val referenceId: String,
    val signature: String,
    val oscpResponse: String,
    val personalNumber: String
)
