package com.hedvig.personservice.persons.domain.commands

import org.axonframework.commandhandling.TargetAggregateIdentifier

data class BlacklistPersonCommand(
    @TargetAggregateIdentifier
    val ssn: String,
    val blacklistedBy: String
)
