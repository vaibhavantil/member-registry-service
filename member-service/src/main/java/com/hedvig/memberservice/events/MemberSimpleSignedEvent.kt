package com.hedvig.memberservice.events

import java.util.UUID

data class MemberSimpleSignedEvent(
    val memberId: Long,
    val ssn: String,
    val referenceId: UUID
)
