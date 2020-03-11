package com.hedvig.memberservice.events

data class NorwegianSSNUpdatedEvent(
    val memberId: Long,
    val ssn: String
)
