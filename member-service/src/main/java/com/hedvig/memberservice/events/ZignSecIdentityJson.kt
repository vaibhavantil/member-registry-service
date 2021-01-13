package com.hedvig.memberservice.events

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.databind.annotation.JsonNaming

abstract class ZignSecIdentityJson {

    abstract val providerJsonResponse: String

    fun parseFirstNameFromZignSecJson(objectMapper: ObjectMapper): String? =
        objectMapper.readValue(providerJsonResponse, ZignSecJson::class.java).identity.firstName

    fun parseLastNameFromZignSecJson(objectMapper: ObjectMapper): String? =
        objectMapper.readValue(providerJsonResponse, ZignSecJson::class.java).identity.lastName

    data class ZignSecJson(
        val identity: ZignSecJsonIdentity
    )

    @JsonNaming(PropertyNamingStrategy.UpperCamelCaseStrategy::class)
    data class ZignSecJsonIdentity(
        val firstName: String?,
        val lastName: String?
    )
}
