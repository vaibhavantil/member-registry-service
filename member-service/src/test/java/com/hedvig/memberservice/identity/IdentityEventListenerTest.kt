package com.hedvig.memberservice.identity

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.hedvig.memberservice.events.NorwegianMemberSignedEvent
import com.hedvig.memberservice.events.ZignSecSuccessfulAuthenticationEvent
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
            repository,
            objectMapper
        )
    }

    @Test
    fun `on NorwegianMemberSignedEvent save IdentityEntity`() {
        every { repository.findById(any()) } returns Optional.empty()
        val slot = CapturingSlot<IdentityEntity>()
        every {
            repository.save(capture(slot))
        } returns stub

        lut.on(
            NorwegianMemberSignedEvent(
                memberId,
                ssn,
                zignSecJson,
                null
            )
        )

        assertThat(slot.captured.firstName).isEqualTo("Test")
        assertThat(slot.captured.lastName).isEqualTo("Testsson")
        assertThat(slot.captured.nationalIdentification.identification).isEqualTo(ssn)
        assertThat(slot.captured.nationalIdentification.nationality).isEqualTo(Nationality.NORWAY)
        assertThat(slot.captured.memberId).isEqualTo(memberId)
    }


    @Test
    fun `on ZignSecSuccessfulAuthenticationEvent save IdentityEntity`() {
        every { repository.findById(any()) } returns Optional.empty()
        val slot = CapturingSlot<IdentityEntity>()
        every {
            repository.save(capture(slot))
        } returns stub

        lut.on(
            ZignSecSuccessfulAuthenticationEvent(
                memberId,
                ssn,
                zignSecJson,
                ZignSecSuccessfulAuthenticationEvent.AuthenticationMethod.DANISH_BANK_ID
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
            ZignSecSuccessfulAuthenticationEvent(
                memberId,
                ssn,
                zignSecJson,
                ZignSecSuccessfulAuthenticationEvent.AuthenticationMethod.DANISH_BANK_ID
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
            ZignSecSuccessfulAuthenticationEvent(
                memberId,
                ssn,
                "{\"identity\": {}}",
                ZignSecSuccessfulAuthenticationEvent.AuthenticationMethod.DANISH_BANK_ID
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
            ZignSecSuccessfulAuthenticationEvent(
                memberId,
                ssn,
                zignSecJson,
                ZignSecSuccessfulAuthenticationEvent.AuthenticationMethod.DANISH_BANK_ID
            )
        )

        verify(exactly = 0) { repository.save(any<IdentityEntity>()) }
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
