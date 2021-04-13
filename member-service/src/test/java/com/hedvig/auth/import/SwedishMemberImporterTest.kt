package com.hedvig.auth.import

import com.hedvig.auth.model.SwedishBankIdCredentialRepository
import com.hedvig.auth.model.UserRepository
import com.hedvig.memberservice.events.MemberSignedEvent
import java.time.Instant
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest

@DataJpaTest
internal class SwedishMemberImporterTest @Autowired constructor(
    private val userRepository: UserRepository,
    private val swedishBankIdCredentialRepository: SwedishBankIdCredentialRepository
) {

    private val importer = SwedishMemberImporter(
        userRepository,
        swedishBankIdCredentialRepository
    )

    @Test
    fun `signed member is imported on event`() {
        val memberId = 123L
        val personalNumber = "201212121212"

        importer.on(
            MemberSignedEvent(memberId, "ref", "sig", "oscp", personalNumber),
            Instant.now()
        )

        val user = userRepository.findByAssociatedMemberId(memberId.toString())
        assertThat(user).isNotNull
        assertThat(user?.swedishBankIdCredential?.personalNumber).isEqualTo(personalNumber)
    }

    @Test
    fun `imported member receives correct timestamp`() {
        val memberId = 123L
        val personalNumber = "201212121212"
        val time = Instant.now().minusSeconds(1000)

        importer.on(
            MemberSignedEvent(memberId, "ref", "sig", "oscp", personalNumber),
            time
        )

        val user = userRepository.findByAssociatedMemberId(memberId.toString())
        assertThat(user?.createdAt).isEqualTo(time)
        assertThat(user?.swedishBankIdCredential?.createdAt).isEqualTo(time)
    }

    @Test
    fun `exporting the same user twice simply ignored second attempt`() {
        val memberId = 123L
        val personalNumber = "201212121212"

        importer.on(
            MemberSignedEvent(memberId, "ref", "sig", "oscp", personalNumber),
            Instant.now()
        )
        importer.on(
            MemberSignedEvent(memberId, "ref", "sig", "oscp", personalNumber),
            Instant.now()
        )

        val users = userRepository.findAll()
        assertThat(users.size).isEqualTo(1)
    }

    @Test
    fun `member signing with the same personal number again should replace the corresponding user`() {
        val memberId1 = 123L
        val memberId2 = 456L
        val personalNumber = "201212121212"
        importer.on(
            MemberSignedEvent(memberId1, "ref", "sig", "oscp", personalNumber),
            Instant.now()
        )
        importer.on(
            MemberSignedEvent(memberId2, "ref", "sig", "oscp", personalNumber),
            Instant.now()
        )

        val users = userRepository.findAll()
        assertThat(users.size).isEqualTo(1)
        assertThat(users[0].associatedMemberId).isEqualTo(memberId2.toString())
        assertThat(users[0].swedishBankIdCredential?.personalNumber).isEqualTo(personalNumber)
    }
}
