package com.hedvig.external.zignSec.client.dto

import com.fasterxml.jackson.annotation.JsonProperty
import java.util.*

data class ZignSecResponse(
    val id: UUID,
    val errors: List<ZignSecResponseError>,
    @JsonProperty("redirect_url")
    val redirectUrl: String?
)
