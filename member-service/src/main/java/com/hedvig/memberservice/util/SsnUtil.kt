package com.hedvig.memberservice.util

interface SsnUtil {
    fun getGenderFromSsn(ssn: String?): Gender?
}

