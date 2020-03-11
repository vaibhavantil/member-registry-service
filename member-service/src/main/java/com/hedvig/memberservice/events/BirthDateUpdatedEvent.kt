package com.hedvig.memberservice.events

import java.time.LocalDate

data class BirthDateUpdatedEvent(
    val memberId: Long,
    val birthDate: LocalDate
)
