package com.hedvig.memberservice.commands

import org.axonframework.commandhandling.TargetAggregateIdentifier

data class SwedishBankIdSignCommand(
    @TargetAggregateIdentifier val id: Long,
    val referenceId: String,
    val signature: String,
    val oscpResponse: String,
    val personalNumber: String
)
