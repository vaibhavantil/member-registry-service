package com.hedvig.personservice.persons.domain.commands

import org.axonframework.commandhandling.TargetAggregateIdentifier

data class CreatePersonCommand(
    @TargetAggregateIdentifier
    val ssn: String
)
