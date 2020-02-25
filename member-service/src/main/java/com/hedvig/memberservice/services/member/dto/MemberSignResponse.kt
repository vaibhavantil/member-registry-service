package com.hedvig.memberservice.services.member.dto

import com.hedvig.external.bankID.bankIdTypes.OrderResponse
import com.hedvig.memberservice.entities.SignStatus
import java.util.*

data class MemberSignResponse(
    val signId: Long? = null,
    val status: SignStatus,
    val bankIdOrderResponse: OrderResponse? = null,
    val norwegianBankIdResponse: NorwegianBankIdResponse? = null
) {
    constructor(
        signId: Long?,
        status: SignStatus,
        bankIdOrderResponse: OrderResponse?
    ): this(
        signId,
        status,
        bankIdOrderResponse,
        null
    )
}
