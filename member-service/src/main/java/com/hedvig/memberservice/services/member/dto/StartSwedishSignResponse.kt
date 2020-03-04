package com.hedvig.memberservice.services.member.dto

import com.hedvig.external.bankID.bankIdTypes.OrderResponse

data class StartSwedishSignResponse(
    val signId: Long,
    val bankIdOrderResponse: OrderResponse
)
