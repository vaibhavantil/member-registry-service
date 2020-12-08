package com.hedvig.memberservice.web.dto

import java.util.UUID

data class UnderwriterStartSimpleSignSessionRequest(
    val underwriterSessionReference: UUID,
    val ssn: String
)
