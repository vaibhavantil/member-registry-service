package com.hedvig.memberservice.events

import com.hedvig.memberservice.web.dto.Market

class MarketUpdatedEvent(
    val memberId: Long,
    val market: Market
)
