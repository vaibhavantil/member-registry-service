package com.hedvig.memberservice.util

import java.time.LocalDate
import java.time.format.DateTimeFormatter

class SsnUtilImpl : SsnUtil {

    override fun getGenderFromSsn(ssn: String?): Gender? {
        if (ssn == null) {
            return null
        }

        val trimmedSSN = ssn.replace("-", "")

        return when (trimmedSSN.length) {
            10 -> {
                //DANISH
                if ((trimmedSSN.toCharArray()[9].toInt() % 2) == 0) {
                    Gender.FEMALE
                } else {
                    Gender.MALE
                }
            }
            11 -> {
                //NORWAY
                if ((trimmedSSN.toCharArray()[8].toInt() % 2) == 0) {
                    Gender.FEMALE
                } else {
                    Gender.MALE
                }
            }
            12 -> {
                //SWEDISH
                if ((trimmedSSN.toCharArray()[10].toInt() % 2) == 0) {
                    Gender.FEMALE
                } else {
                    Gender.MALE
                }
            }
            else -> null
        }
    }

    companion object {
        @JvmStatic
        val instance: SsnUtil = SsnUtilImpl()

        fun getBirthdateFromSwedishSsn(ssn: String?): LocalDate? {
            if (ssn != null) {
                val dtf = DateTimeFormatter.ofPattern("yyyyMMdd")
                return LocalDate.parse(ssn.substring(0, 8), dtf)
            }
            return null
        }
    }
}
