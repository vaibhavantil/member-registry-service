package com.hedvig.personservice.persons.web.dtos

data class HasPersonSignedBeforeRequest(
    val ssn: String?,
    val email: String
)
