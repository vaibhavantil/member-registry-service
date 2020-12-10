package com.hedvig.memberservice.commands

import org.axonframework.commandhandling.TargetAggregateIdentifier
import java.time.LocalDate

data class UpdateBirthDateCommand(
    @TargetAggregateIdentifier
    val memberId: Long,
    val birthDate: LocalDate
)
