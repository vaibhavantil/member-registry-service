package com.hedvig.external.zignSec.client.dto

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL

data class ZignSecRequestBody(
    @JsonInclude(NON_NULL)
    val personalnumber: String?,
    val language: String,
    val target: String,
    val targetError: String,
    val webhook: String
)
