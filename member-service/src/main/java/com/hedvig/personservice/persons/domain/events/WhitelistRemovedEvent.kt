package com.hedvig.personservice.persons.domain.events

data class WhitelistRemovedEvent(
    val ssn: String,
    val removedBy: String
)
