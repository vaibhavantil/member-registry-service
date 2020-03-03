package com.hedvig.memberservice.commands

import com.hedvig.memberservice.web.dto.Market
import org.axonframework.commandhandling.TargetAggregateIdentifier

data class UpdateMarketCommand(
    @TargetAggregateIdentifier val memberId: Long,
    val market: Market
)
