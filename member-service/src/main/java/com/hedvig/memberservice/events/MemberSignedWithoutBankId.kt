package com.hedvig.memberservice.events

data class MemberSignedWithoutBankId(
    val memberId: Long,
    val ssn: String
)
