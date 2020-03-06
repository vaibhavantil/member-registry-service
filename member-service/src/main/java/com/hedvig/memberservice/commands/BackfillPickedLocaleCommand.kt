package com.hedvig.memberservice.commands

import org.axonframework.commandhandling.TargetAggregateIdentifier

data class BackfillPickedLocaleCommand(
    @TargetAggregateIdentifier val memberId: Long
)
