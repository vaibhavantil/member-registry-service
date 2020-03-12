package com.hedvig.memberservice.web.dto

import java.util.*

data class UnderwriterStartSwedishBankIdSignSessionRequest(
    val underwriterSessionReference: UUID,
    val ssn: String,
    val ipAddress: String,
    val isSwitching: Boolean
)
