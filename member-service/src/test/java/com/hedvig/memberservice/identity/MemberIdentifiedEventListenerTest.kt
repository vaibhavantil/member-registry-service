package com.hedvig.memberservice.identity

import com.hedvig.memberservice.events.MemberIdentifiedEvent
import com.hedvig.memberservice.identity.models.MemberIdentityRevision
import com.hedvig.memberservice.identity.models.MemberIdentityRevisionRepository
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.context.annotation.Import

@DataJpaTest
@Import(MemberIdentifiedEventListener::class)
internal class MemberIdentifiedEventListenerTest(
    val listener: MemberIdentifiedEventListener,
    val repository: MemberIdentityRevisionRepository
) {

    @Test
    fun `identity is exported on event`() {
        listener.on(
            MemberIdentifiedEvent(
                memberId = 123L,
                nationalIdentification = MemberIdentifiedEvent.NationalIdentification(
                    identification = "201212121212",
                    nationality = MemberIdentifiedEvent.Nationality.SWEDEN
                ),
                identificationMethod = MemberIdentifiedEvent.IdentificationMethod("BankIDSE"),
                firstName = "Test",
                lastName = "Testsson"
            )
        )

        val revisions = repository.findByMemberId(123L)
        assertThat(revisions.size).isEqualTo(1)
        assertThat(revisions[0].firstName).isEqualTo("Test")
        assertThat(revisions[0].lastName).isEqualTo("Testsson")
        assertThat(revisions[0].nationalIdentifier).isEqualTo("201212121212")
        assertThat(revisions[0].countryCode).isEqualTo("SE")
        assertThat(revisions[0].identificationSource).isEqualTo("BankIDSE")
    }

    @Test
    fun `identity is not exported again if the data is the same`() {
        val event = MemberIdentifiedEvent(
            memberId = 123L,
            nationalIdentification = MemberIdentifiedEvent.NationalIdentification(
                identification = "201212121212",
                nationality = MemberIdentifiedEvent.Nationality.SWEDEN
            ),
            identificationMethod = MemberIdentifiedEvent.IdentificationMethod("BankIDSE"),
            firstName = "Test",
            lastName = "Testsson"
        )

        listener.on(event)
        listener.on(event)

        val revisions = repository.findByMemberId(123L)
        assertThat(revisions.size).isEqualTo(1)
    }

    @Test
    fun `identity is exported again if the data differs`() {
        val event = MemberIdentifiedEvent(
            memberId = 123L,
            nationalIdentification = MemberIdentifiedEvent.NationalIdentification(
                identification = "201212121212",
                nationality = MemberIdentifiedEvent.Nationality.SWEDEN
            ),
            identificationMethod = MemberIdentifiedEvent.IdentificationMethod("BankIDSE"),
            firstName = "Test",
            lastName = "Testsson"
        )

        listener.on(event)
        listener.on(event.copy(firstName = "Test2"))

        val revisions = repository.findByMemberId(123L)
        assertThat(revisions.size).isEqualTo(2)
        assertThat(revisions[0].firstName).isEqualTo("Test")
        assertThat(revisions[1].firstName).isEqualTo("Test2")
    }
}
