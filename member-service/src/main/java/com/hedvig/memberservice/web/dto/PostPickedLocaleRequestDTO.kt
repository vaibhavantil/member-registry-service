package com.hedvig.memberservice.web.dto

import com.hedvig.memberservice.aggregates.PickedLocale

data class PostPickedLocaleRequestDTO (
     val pickedLocale : PickedLocale
)
