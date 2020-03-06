package com.hedvig.memberservice.events

import com.hedvig.memberservice.aggregates.PickedLocale


class PickedLocaleUpdatedEvent(
    val memberId: Long,
    val pickedLocale: PickedLocale
)
