package com.hedvig.personservice.persons.domain.commands

import com.hedvig.personservice.debts.model.DebtSnapshot
import org.axonframework.commandhandling.TargetAggregateIdentifier

data class SynaDebtCheckedCommand(
        @TargetAggregateIdentifier
        val ssn: String,
        val debtSnapshot: DebtSnapshot
)
