package com.hedvig.personservice.persons.domain.events

data class PersonWhitelistedEvent(
    val ssn: String,
    val whitelistedBy: String
)
