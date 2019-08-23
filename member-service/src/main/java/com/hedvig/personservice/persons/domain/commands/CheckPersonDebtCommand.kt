package com.hedvig.personservice.persons.domain.commands

import org.axonframework.commandhandling.TargetAggregateIdentifier

data class CheckPersonDebtCommand(
    @TargetAggregateIdentifier
    val ssn: String
)
