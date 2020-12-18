package com.hedvig.integration.notificationService.dto

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "eventName")
@JsonSubTypes(
    JsonSubTypes.Type(value = PhoneNumberUpdatedEvent::class, name = "PhoneNumberUpdatedEvent")
)
sealed class EventRequest

data class PhoneNumberUpdatedEvent(
    val memberId: String,
    val phoneNumber: String
) : EventRequest()
