package com.hedvig.personservice

fun safeSsn(ssn: String): String = "${ssn.substring(0, 10)}-XXXX"
