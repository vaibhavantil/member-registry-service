package com.hedvig.auth.import

import com.hedvig.auth.model.SwedishBankIdCredentialRepository
import com.hedvig.auth.model.UserRepository
import com.hedvig.memberservice.events.MemberSignedEvent
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
    fun `signed member is exported on event`() {
        val memberId = 123L
        val personalNumber = "201212121212"

        importer.on(
            MemberSignedEvent(memberId, "ref", "sig", "oscp", personalNumber)
        )

        val user = userRepository.findByAssociatedMemberId(memberId.toString())
        assertThat(user).isNotNull
        assertThat(user?.swedishBankIdCredential?.personalNumber).isEqualTo(personalNumber)
    }

    @Test
    fun `exporting the same user twice simply ignored second attempt`() {
        val memberId = 123L
        val personalNumber = "201212121212"

        importer.on(
            MemberSignedEvent(memberId, "ref", "sig", "oscp", personalNumber)
        )
        importer.on(
            MemberSignedEvent(memberId, "ref", "sig", "oscp", personalNumber)
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
            MemberSignedEvent(memberId1, "ref", "sig", "oscp", personalNumber)
        )
        importer.on(
            MemberSignedEvent(memberId2, "ref", "sig", "oscp", personalNumber)
        )

        val users = userRepository.findAll()
        assertThat(users.size).isEqualTo(1)
        assertThat(users[0].associatedMemberId).isEqualTo(memberId2.toString())
        assertThat(users[0].swedishBankIdCredential?.personalNumber).isEqualTo(personalNumber)
    }
}
