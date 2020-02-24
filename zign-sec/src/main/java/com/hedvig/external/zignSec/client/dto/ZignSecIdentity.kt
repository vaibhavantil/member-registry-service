package com.hedvig.external.zignSec.client.dto

import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.databind.annotation.JsonNaming
import java.time.LocalDateTime

@JsonNaming(PropertyNamingStrategy.UpperCamelCaseStrategy::class)
data class ZignSecIdentity(
    val countryCode: String?,
    val firstName: String?,
    val lastName: String?,
    val fullName: String?,
    val personalNumber: String?,
    val dateOfBirth: String?,
    val age: Int?,
    val gender: String?,
    val idProviderName: String?,
    val identificationDate: LocalDateTime?,
    val idProviderRequestId: String?,
    val idProviderPersonId: String?,
    val customerPersonId: String?
)
