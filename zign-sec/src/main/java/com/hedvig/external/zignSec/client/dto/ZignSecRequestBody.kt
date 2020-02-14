package com.hedvig.external.zignSec.client.dto

import java.util.*

data class ZignSecRequestBody(
    val personalnumber: String,
    val language: String,
    val target: String = "https://s3.eu-central-1.amazonaws.com/com-hedvig-web-content/trustly/success.html", // TODO: should probably be something else
    val targetError: String = "https://s3.eu-central-1.amazonaws.com/com-hedvig-web-content/trustly/fail.html", // TODO: should probably be something else
    val webhook: String // TODO: set this up!
)
