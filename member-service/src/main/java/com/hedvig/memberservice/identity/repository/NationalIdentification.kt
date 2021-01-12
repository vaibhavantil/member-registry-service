package com.hedvig.memberservice.identity.repository

import javax.persistence.Embeddable
import javax.persistence.EnumType
import javax.persistence.Enumerated

@Embeddable
class NationalIdentification(
    val identification: String,
    @Enumerated(EnumType.STRING)
    val nationality: Nationality
)
