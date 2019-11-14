package com.hedvig.personservice.persons.domain.events

data class PersonBlacklistedEvent(
    val ssn: String,
    val blacklistedBy: String
)
