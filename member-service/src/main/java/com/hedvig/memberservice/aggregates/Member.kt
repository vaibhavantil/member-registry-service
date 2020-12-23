package com.hedvig.memberservice.aggregates

import java.time.LocalDate

data class Member(
    val firstName: String? = null,
    val lastName: String? = null,
    val ssn: String? = null,
    val email: String? = null,
    val phoneNumber: String? = null,
    val acceptLanguage: String? = null,
    val pickedLocale: PickedLocale? = null,
    val livingAddress: LivingAddress? = null,
    val birthDate: LocalDate? = null
)
