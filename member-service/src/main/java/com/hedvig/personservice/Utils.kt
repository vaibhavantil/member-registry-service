package com.hedvig.personservice

fun maskLastDigitsOfSsn(ssn: String): String = "${ssn.substring(0, 10)}-XXXX"
