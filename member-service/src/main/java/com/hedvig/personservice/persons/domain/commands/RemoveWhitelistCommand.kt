package com.hedvig.personservice.persons.domain.commands

import org.axonframework.commandhandling.TargetAggregateIdentifier

data class RemoveWhitelistCommand(
    @TargetAggregateIdentifier
    val ssn: String,
    val removedBy: String
)
