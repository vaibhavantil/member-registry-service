package com.hedvig.memberservice.identity.repository

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class IdentityEntityTest {

    @Test
    fun `update with null first and last name doesn't override`(){
        val identityEntity = IdentityEntity(
            1234L,
            NationalIdentification(
                "ssn",
                Nationality.NORWAY
            ),
            IdentificationMethod.NORWEGIAN_BANK_ID,
            "firstName",
            "lastName"
        )

        val result = identityEntity.update(
            IdentityEntity(
                1234L,
                NationalIdentification(
                    "new",
                    Nationality.NORWAY
                ),
                IdentificationMethod.NORWEGIAN_BANK_ID,
                null,
                null
            )
        )

        assertThat(result.firstName).isEqualTo("firstName")
        assertThat(result.lastName).isEqualTo("lastName")
    }
}
