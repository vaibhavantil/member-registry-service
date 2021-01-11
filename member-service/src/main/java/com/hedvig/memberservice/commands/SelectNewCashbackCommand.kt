package com.hedvig.memberservice.commands

import org.axonframework.commandhandling.TargetAggregateIdentifier
import java.util.UUID

data class SelectNewCashbackCommand(
    @TargetAggregateIdentifier
    val memberId: Long,
    val optionId: UUID
)
