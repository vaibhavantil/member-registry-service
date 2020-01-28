package com.hedvig.memberservice.util

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

private const val maleSsn = "19950929-6233"
private const val femaleSsn = "20121212-3286"

class SsnUtilImplTest {
    private val ssnUtil = SsnUtilImpl()
    @Test
    fun getsGender() {
        assertThat(ssnUtil.getGenderFromSsn(maleSsn)).isEqualTo(Gender.MALE)
        assertThat(ssnUtil.getGenderFromSsn(femaleSsn)).isEqualTo(Gender.FEMALE)
    }
}
