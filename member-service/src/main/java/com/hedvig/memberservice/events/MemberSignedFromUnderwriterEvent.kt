package com.hedvig.memberservice.events

data class MemberSignedFromUnderwriterEvent (
    val memberId: Long,
    val ssn: String
)
