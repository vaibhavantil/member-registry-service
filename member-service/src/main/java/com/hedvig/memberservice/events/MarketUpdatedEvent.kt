package com.hedvig.memberservice.events

import com.hedvig.memberservice.aggregates.Market


class MarketUpdatedEvent(
    val memberId: Long,
    val market: Market
)
