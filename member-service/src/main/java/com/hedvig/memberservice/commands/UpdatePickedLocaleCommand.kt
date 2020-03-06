package com.hedvig.memberservice.commands

import com.hedvig.memberservice.aggregates.PickedLocale
import org.axonframework.commandhandling.TargetAggregateIdentifier

data class UpdatePickedLocaleCommand(
    @TargetAggregateIdentifier val memberId: Long,
    val pickedLocale: PickedLocale
)
