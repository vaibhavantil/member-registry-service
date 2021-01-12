package com.hedvig.memberservice.services

import com.hedvig.memberservice.identity.repository.IdentificationMethod
import com.hedvig.memberservice.identity.repository.IdentityEntity
import com.hedvig.memberservice.identity.repository.IdentityRepository
import com.hedvig.memberservice.identity.repository.NationalIdentification
import com.hedvig.memberservice.identity.repository.Nationality
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.util.Optional

class IdentityServiceImplTest {

    private val repository: IdentityRepository = mockk()

    private val sut = IdentityServiceImpl(repository)

    @Test
    fun `on identified member get identity`() {
        every { repository.findById(any()) } returns Optional.of(IdentityEntity(
            memberId,
            NationalIdentification(
                "ssn",
                Nationality.NORWAY
            ),
            IdentificationMethod.NORWEGIAN_BANK_ID,
            "firstName",
            "lastName"
        ))

        val result = sut.identityOnMemberId(memberId)

        assertThat(result).isNotNull
    }


    @Test
    fun `on not identified member null`() {
        every { repository.findById(any()) } returns Optional.empty()

        val result = sut.identityOnMemberId(memberId)

        assertThat(result).isNull()
    }

    companion object {
        val memberId = 1234L
    }
}
