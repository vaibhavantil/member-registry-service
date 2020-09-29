package com.hedvig.memberservice.services.member.dto

import com.hedvig.external.bankID.bankIdTypes.OrderResponse
import com.hedvig.memberservice.entities.SignStatus

data class MemberSignResponse(
    val signId: Long? = null,
    val status: SignStatus,
    val bankIdOrderResponse: OrderResponse? = null,
    //TODO: have to look att this one, is it used by giraffe? or underwriter? Should we have a danish one or should it be generifyed
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
