package com.hedvig.personservice.persons.domain.events

import java.util.*

data class PersonCreatedEvent(
    val id: UUID,
    val ssn: String
)
