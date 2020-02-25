package com.hedvig.external.zignSec.client.dto

data class ZignSecRequestBody(
    val personalnumber: String,
    val language: String,
    val target: String,
    val targetError: String,
    val webhook: String
)
