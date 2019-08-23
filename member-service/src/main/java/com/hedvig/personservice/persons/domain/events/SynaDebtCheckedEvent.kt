package com.hedvig.personservice.persons.domain.events

import com.hedvig.personservice.debts.model.DebtSnapshot

data class SynaDebtCheckedEvent(
    val ssn: String,
    val debtSnapshot: DebtSnapshot
)
