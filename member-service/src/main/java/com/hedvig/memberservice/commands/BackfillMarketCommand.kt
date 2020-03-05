package com.hedvig.memberservice.commands

import org.axonframework.commandhandling.TargetAggregateIdentifier

data class BackfillMarketCommand(
    @TargetAggregateIdentifier val memberId: Long
)
