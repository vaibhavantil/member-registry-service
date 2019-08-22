package com.hedvig.personservice.persons.domain.events

import java.util.*

data class CheckPersonDebtEvent(val personId: UUID, val ssn: String)
