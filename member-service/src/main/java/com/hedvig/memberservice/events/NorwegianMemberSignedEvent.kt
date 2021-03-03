package com.hedvig.memberservice.events

import java.util.UUID

data class NorwegianMemberSignedEvent(
    val memberId: Long,
    val ssn: String,
    val providerJsonResponse: String,
    val referenceId: UUID
)

