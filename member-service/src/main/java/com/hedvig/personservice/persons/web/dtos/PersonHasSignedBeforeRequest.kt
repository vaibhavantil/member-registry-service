package com.hedvig.personservice.persons.web.dtos

data class PersonHasSignedBeforeRequest(
    val ssn: String?,
    val email: String
)
