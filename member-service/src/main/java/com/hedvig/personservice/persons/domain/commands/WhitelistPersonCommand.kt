package com.hedvig.personservice.persons.domain.commands

import org.axonframework.commandhandling.TargetAggregateIdentifier

data class WhitelistPersonCommand(
    @TargetAggregateIdentifier
    val ssn: String,
    val whitelistedBy: String
)
