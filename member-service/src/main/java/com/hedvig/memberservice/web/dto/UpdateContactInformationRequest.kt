package com.hedvig.memberservice.web.dto

import java.time.LocalDate

class UpdateContactInformationRequest(
    val memberId: String,
    val firstName: String? = null,
    val lastName: String? = null,
    val email: String? = null,
    val phoneNumber: String? = null,
    val address: Address? = null,
    val birthDate: LocalDate? = null
)
