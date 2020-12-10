package com.hedvig.memberservice.web.dto

import java.util.UUID

data class UnderwriterStartRedirectBankIdSignSessionRequest(
    val underwriterSessionReference: UUID,
    val ssn: String,
    val successUrl: String,
    val failUrl: String
)
