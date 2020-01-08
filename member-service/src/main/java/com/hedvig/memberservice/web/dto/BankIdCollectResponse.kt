package com.hedvig.memberservice.web.dto

data class BankIdCollectResponse(
    val bankIdStatus: BankIdProgressStatus,
    val referenceToken: String,
    val newMemberId: String
)
