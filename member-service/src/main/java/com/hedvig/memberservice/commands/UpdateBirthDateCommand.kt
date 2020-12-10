package com.hedvig.memberservice.commands

import java.time.LocalDate

data class UpdateBirthDateCommand(
    val memberId: Long,
    val birthDate: LocalDate
)
