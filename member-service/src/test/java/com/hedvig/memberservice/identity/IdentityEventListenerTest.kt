package com.hedvig.memberservice.identity

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.hedvig.memberservice.events.MemberDeletedEvent
import com.hedvig.memberservice.events.NorwegianMemberSignedEvent
import com.hedvig.memberservice.events.MemberIdentifiedEvent
import com.hedvig.memberservice.identity.repository.IdentificationMethod
import com.hedvig.memberservice.identity.repository.IdentityEntity
import com.hedvig.memberservice.identity.repository.IdentityRepository
import com.hedvig.memberservice.identity.repository.NationalIdentification
import com.hedvig.memberservice.identity.repository.Nationality
import io.mockk.CapturingSlot
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.Optional

class IdentityEventListenerTest {

    val repository: IdentityRepository = mockk()
    lateinit var objectMapper: ObjectMapper

    lateinit var lut: IdentityEventListener

    @BeforeEach
    fun before() {
        objectMapper = ObjectMapper().registerKotlinModule().registerModule(JavaTimeModule())
        lut = IdentityEventListener(
            repository
        )
    }

    @Test
    fun `on MemberIdentifiedEvent save IdentityEntity`() {
        every { repository.findById(any()) } returns Optional.empty()
        val slot = CapturingSlot<IdentityEntity>()
        every {
            repository.save(capture(slot))
        } returns stub

        lut.on(
            MemberIdentifiedEvent(
                memberId,
                MemberIdentifiedEvent.NationalIdentification(
                    ssn,
                    MemberIdentifiedEvent.Nationality.DENMARK
                ),
                MemberIdentifiedEvent.IdentificationMethod.DANISH_BANK_ID,
                "Test",
                "Testsson"
            )
        )

        assertThat(slot.captured.firstName).isEqualTo("Test")
        assertThat(slot.captured.lastName).isEqualTo("Testsson")
        assertThat(slot.captured.nationalIdentification.identification).isEqualTo(ssn)
        assertThat(slot.captured.nationalIdentification.nationality).isEqualTo(Nationality.DENMARK)
        assertThat(slot.captured.memberId).isEqualTo(memberId)
    }

    @Test
    fun `save new entity with name if old has no name`() {
        val oldIdentityEntity = IdentityEntity(
            memberId,
            NationalIdentification(
                ssn,
                Nationality.NORWAY
            ),
            IdentificationMethod.NORWEGIAN_BANK_ID,
            null,
            null
        )
        every { repository.findById(any()) } returns Optional.of(oldIdentityEntity)
        every {
            repository.save(any<IdentityEntity>())
        } returns stub

        lut.on(
            MemberIdentifiedEvent(
                memberId,
                MemberIdentifiedEvent.NationalIdentification(
                    ssn,
                    MemberIdentifiedEvent.Nationality.DENMARK
                ),
                MemberIdentifiedEvent.IdentificationMethod.DANISH_BANK_ID,
                "Test",
                "Testsson"
            )
        )

        verify(exactly = 1) { repository.save(any<IdentityEntity>()) }
    }

    @Test
    fun `on event without name is not overridden`() {
        val oldIdentityEntity = IdentityEntity(
            memberId,
            NationalIdentification(
                ssn,
                Nationality.NORWAY
            ),
            IdentificationMethod.NORWEGIAN_BANK_ID,
            "Test",
            "Testsson"
        )
        every { repository.findById(any()) } returns Optional.of(oldIdentityEntity)
        val slot = CapturingSlot<IdentityEntity>()
        every {
            repository.save(capture(slot))
        } returns stub

        lut.on(
            MemberIdentifiedEvent(
                memberId,
                MemberIdentifiedEvent.NationalIdentification(
                    ssn,
                    MemberIdentifiedEvent.Nationality.DENMARK
                ),
                MemberIdentifiedEvent.IdentificationMethod.DANISH_BANK_ID,
                null,
                null
            )
        )

        verify(exactly = 1) { repository.save(any<IdentityEntity>()) }
        assertThat(slot.captured.firstName).isEqualTo("Test")
        assertThat(slot.captured.lastName).isEqualTo("Testsson")
    }

    @Test
    fun `on event with no new information is not saved`() {
        val oldIdentityEntity = IdentityEntity(
            memberId,
            NationalIdentification(
                ssn,
                Nationality.NORWAY
            ),
            IdentificationMethod.NORWEGIAN_BANK_ID,
            "Test",
            "Testsson"
        )
        every { repository.findById(any()) } returns Optional.of(oldIdentityEntity)

        lut.on(
            MemberIdentifiedEvent(
                memberId,
                MemberIdentifiedEvent.NationalIdentification(
                    ssn,
                    MemberIdentifiedEvent.Nationality.DENMARK
                ),
                MemberIdentifiedEvent.IdentificationMethod.DANISH_BANK_ID,
                "Test",
                "Testsson"
            )
        )

        verify(exactly = 0) { repository.save(any<IdentityEntity>()) }
    }

    @Test
    fun `should delete identity entity on MemberDeletedEvent`() {
        every {
            repository.deleteById(memberId)
        } returns Unit
        lut.on(MemberDeletedEvent(memberId))
        verify(exactly = 1) { repository.deleteById(memberId) }
    }

    companion object {
        val memberId = 1234L
        val ssn = "12121212120"
        val zignSecJson = """
            {
                "identity": {
                    "FirstName":"Test",
                    "LastName":"Testsson"
                }
            }
        """.trimIndent()
        val stub = IdentityEntity(
            1L,
            NationalIdentification(
                "",
                Nationality.NORWAY
            ),
            IdentificationMethod.NORWEGIAN_BANK_ID,
            "",
            ""
        )
    }
}
