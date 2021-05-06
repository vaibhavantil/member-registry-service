package com.hedvig.external.zignSec.client.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import java.util.*

@JsonIgnoreProperties(ignoreUnknown = true)
data class ZignSecNotificationRequest(
    val id: UUID,
    val errors: List<ZignSecResponseError>,
    val identity: ZignSecIdentity?,
    val method: String?,
    @JsonProperty("BANKIdNO_OIDC")
    val bankIdNoOidc: String?
)
