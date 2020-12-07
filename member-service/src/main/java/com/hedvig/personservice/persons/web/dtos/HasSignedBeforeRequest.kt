package com.hedvig.personservice.persons.web.dtos

data class HasSignedBeforeRequest(
    val memberId: String,
    val ssn: String?,
    val email: String
)
