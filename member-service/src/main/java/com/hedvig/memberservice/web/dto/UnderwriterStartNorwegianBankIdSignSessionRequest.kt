package com.hedvig.memberservice.web.dto

import java.util.*

data class UnderwriterStartNorwegianBankIdSignSessionRequest(
    val underwriterSessionReference: UUID,
    val ssn: String
)
