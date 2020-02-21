package com.hedvig.memberservice.util

interface SsnUtil {
    fun getMarketFromSsn(ssn: String): Market
    fun getGenderFromSsn(ssn: String?): Gender?
}

