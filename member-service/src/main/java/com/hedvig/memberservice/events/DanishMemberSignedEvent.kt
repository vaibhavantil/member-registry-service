package com.hedvig.memberservice.events

import java.util.*

data class DanishMemberSignedEvent(
    val memberId: Long,
    val ssn: String,
    val providerJsonResponse: String,
    val referenceId: UUID
)
