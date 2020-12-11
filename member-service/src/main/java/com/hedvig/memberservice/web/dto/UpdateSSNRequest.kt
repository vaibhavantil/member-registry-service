package com.hedvig.memberservice.web.dto

data class UpdateSSNRequest(
    val ssn: String,
    val nationality: Nationality
)

