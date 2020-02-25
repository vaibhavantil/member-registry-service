package com.hedvig.external.zignSec.client.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.util.*

@JsonIgnoreProperties(ignoreUnknown = true)
data class ZignSecCollectResponse(
    val id: UUID,
    val result: ZignSecCollectResult
)

