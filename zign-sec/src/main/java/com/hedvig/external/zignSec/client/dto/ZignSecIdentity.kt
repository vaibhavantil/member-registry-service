package com.hedvig.external.zignSec.client.dto

import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.databind.annotation.JsonNaming
import java.time.LocalDateTime

@JsonNaming(PropertyNamingStrategy.UpperCamelCaseStrategy::class)
data class ZignSecIdentity(
    val countryCode: String? = null,
    val firstName: String? = null,
    val lastName: String? = null,
    val fullName: String? = null,
    val personalNumber: String? = null,
    val dateOfBirth: String? = null,
    val age: Int? = null,
    val gender: String? = null,
    val idProviderName: String? = null,
    val identificationDate: LocalDateTime? = null,
    val idProviderRequestId: String? = null,
    val idProviderPersonId: String? = null,
    val customerPersonId: String? = null
)
