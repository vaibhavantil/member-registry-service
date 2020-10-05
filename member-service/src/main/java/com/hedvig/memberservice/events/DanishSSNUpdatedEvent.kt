package com.hedvig.memberservice.events

data class DanishSSNUpdatedEvent(
    val memberId: Long,
    val ssn: String
)
