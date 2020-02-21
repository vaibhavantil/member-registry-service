package com.hedvig.memberservice.events

data class NorwegianMemberSignedEvent (
    val memberId: Long,
    val ssn: String,
    val providerJsonResponse: String
)
