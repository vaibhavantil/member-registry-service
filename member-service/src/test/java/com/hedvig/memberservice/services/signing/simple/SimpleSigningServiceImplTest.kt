package com.hedvig.memberservice.services.signing.simple

import com.hedvig.memberservice.commands.MemberSimpleSignedCommand
import com.hedvig.memberservice.services.signing.simple.dto.SimpleSignStatus
import com.hedvig.memberservice.services.signing.simple.repository.SimpleSignSession
import com.hedvig.memberservice.services.signing.simple.repository.SimpleSigningSessionRepository
import com.hedvig.memberservice.web.dto.NationalIdentification
import com.hedvig.memberservice.web.dto.Nationality
import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.axonframework.commandhandling.gateway.CommandGateway
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.util.UUID

@ExtendWith(MockKExtension::class)
class SimpleSigningServiceImplTest {

    private val repository: SimpleSigningSessionRepository = mockk()
    private val commandGateway: CommandGateway = mockk(relaxed = true)

    private val cut = SimpleSigningServiceImpl(repository, commandGateway)

    @Test
    fun `start sign should store simple sign session`() {
        val memberId = 1234L
        val nationalIdentification = NationalIdentification("any ssn", Nationality.SWEDEN)

        val slot = slot<SimpleSignSession>()
        every {
            repository.save(capture(slot))
        } returns SimpleSignSession(UUID.randomUUID(), memberId, nationalIdentification.identification, nationalIdentification.nationality)

        val result = cut.startSign(memberId, nationalIdentification)

        verify { commandGateway.sendAndWait(MemberSimpleSignedCommand(memberId, nationalIdentification, result)) }
        assertThat(slot.captured.memberId).isEqualTo(memberId)
        assertThat(slot.captured.nationalIdentification).isEqualTo(nationalIdentification.identification)
        assertThat(slot.captured.nationality).isEqualTo(nationalIdentification.nationality)
        assertThat(slot.captured.isContractsCreated).isEqualTo(false)
    }

    @Test
    fun `notify contracts created should update isContractsCreated to true`() {
        val memberId = 1234L
        val nationalIdentification = NationalIdentification("any ssn", Nationality.SWEDEN)

        val slot = slot<SimpleSignSession>()
        every {
            repository.findByMemberId(memberId)
        } returns SimpleSignSession(UUID.randomUUID(), memberId, nationalIdentification.identification, nationalIdentification.nationality)

        every {
            repository.save(capture(slot))
        } returns SimpleSignSession(UUID.randomUUID(), memberId, nationalIdentification.identification, nationalIdentification.nationality)

        cut.notifyContractsCreated(memberId)

        assertThat(slot.captured.isContractsCreated).isEqualTo(true)
    }

    @Test
    fun `sign status is CONTRACTS_CREATED if contract is created`() {
        val memberId = 1234L
        val nationalIdentification = NationalIdentification("any ssn", Nationality.SWEDEN)

        every {
            repository.findByMemberId(memberId)
        } returns SimpleSignSession(UUID.randomUUID(), memberId, nationalIdentification.identification, nationalIdentification.nationality, true)

        val status = cut.getSignStatus(memberId)

        assertThat(status).isEqualTo(SimpleSignStatus.CONTRACTS_CREATED)
    }

    @Test
    fun `sign status is INITIATED if contract is not created`() {
        val memberId = 1234L
        val nationalIdentification = NationalIdentification("any ssn", Nationality.SWEDEN)

        every {
            repository.findByMemberId(memberId)
        } returns SimpleSignSession(UUID.randomUUID(), memberId, nationalIdentification.identification, nationalIdentification.nationality, false)

        val status = cut.getSignStatus(memberId)

        assertThat(status).isEqualTo(SimpleSignStatus.INITIATED)
    }

    @Test
    fun `sign status is null if there is no session`() {
        val memberId = 1234L

        every {
            repository.findByMemberId(memberId)
        } returns null

        val status = cut.getSignStatus(memberId)

        assertThat(status).isEqualTo(null)
    }
}
