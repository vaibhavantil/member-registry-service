package com.hedvig.memberservice.util

class SsnUtilImpl : SsnUtil {

    override fun getMarketFromSsn(ssn: String): Market = when (ssn.length) {
        12 -> Market.SWEDEN
        11 -> Market.NORWAY
        else -> throw IllegalArgumentException("Could not get market from ssn [ssn: $ssn]")
    }


    override fun getGenderFromSsn(ssn: String?): Gender? {
        if (ssn == null) {
            return null
        }

        return if ((ssn.replace("-", "").toCharArray()[10].toInt() % 2) == 0) {
            Gender.FEMALE
        } else {
            Gender.MALE
        }
    }

    companion object {
        @JvmStatic
        val instance: SsnUtil = SsnUtilImpl()
    }
}
