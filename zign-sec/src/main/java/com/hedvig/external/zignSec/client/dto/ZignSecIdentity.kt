package com.hedvig.external.zignSec.client.dto

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.LocalDate

data class ZignSecIdentity(
    @JsonProperty("CountryCode")
    val countryCode: String?,
    @JsonProperty("FirstName")
    val firstName: String?,
    @JsonProperty("LastName")
    val lastName: String?,
    @JsonProperty("FullName")
    val fullName: String?,
    @JsonProperty("PersonalNumber")
    val personalNumber: String?,
    @JsonProperty("DateOfBirth")
    val dateOfBirth: String?,
    @JsonProperty("Age")
    val age: Int?,
    @JsonProperty("Gender")
    val gender: String?,
    @JsonProperty("IdProviderName")
    val idProviderName: String?,
    @JsonProperty("IdentificationDate")
    val identificationDate: LocalDate,
    @JsonProperty("IdProviderRequestId")
    val idProviderRequestId: String?,
    @JsonProperty("IdProviderPersonId")
    val idProviderPersonId: String?,
    @JsonProperty("CustomerPersonId")
    val customerPersonId: String?
)
