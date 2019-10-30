package com.hedvig.memberservice.events

class AcceptLanguageUpdatedEvent(
    val memberId: Long,
    val acceptLanguage: String
)