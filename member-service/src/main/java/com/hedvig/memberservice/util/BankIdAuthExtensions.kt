package com.hedvig.memberservice.util

import org.slf4j.LoggerFactory

private val log = LoggerFactory.getLogger("BankIdAuthExtensionsLogger")

fun String?.getEndUserIp(errorMsg: String): String = if (this?.contains(",") == true) {
    this.split(",").first()
} else {
    this ?: run {
        log.error(errorMsg)
        "127.0.0.1"
    }
}
