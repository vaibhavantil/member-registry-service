package com.hedvig.memberservice.util

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
    }
}
