package com.hedvig.memberservice.util

class SsnUtilImpl : SsnUtil {
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
