package com.hedvig.auth.import

import com.hedvig.auth.models.AuditEvent
import com.hedvig.auth.models.AuditEventRepository
import com.hedvig.auth.models.SwedishBankIdCredential
import com.hedvig.auth.models.SwedishBankIdCredentialRepository
import com.hedvig.auth.models.User
import com.hedvig.auth.models.UserRepository
import com.hedvig.memberservice.events.MemberSignedEvent
import com.hedvig.memberservice.events.MemberSignedWithoutBankId
import com.hedvig.memberservice.events.MemberSimpleSignedEvent
import java.time.Instant
import java.util.UUID
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager
import org.springframework.context.ApplicationContext

@DataJpaTest
internal class SwedishRepairingSimpleSignMemberImporterTest @Autowired constructor(
    private val userRepository: UserRepository,
    private val swedishBankIdCredentialRepository: SwedishBankIdCredentialRepository,
    private val auditEventRepository: AuditEventRepository,
    private val entityManager: TestEntityManager
) {

    private lateinit var importer: SwedishRepairingSimpleSignMemberImporter

    @BeforeEach
    fun setup(@Autowired context: ApplicationContext) {
        importer = context.autowireCapableBeanFactory.createBean(SwedishRepairingSimpleSignMemberImporter::class.java)
    }

    @Test
    fun `signed member is imported on event`() {
        val memberId = 123L
        val personalNumber = "201212121212"

        importer.on(
            MemberSignedWithoutBankId(memberId, personalNumber),
            Instant.now()
        )

        val user = userRepository.findByAssociatedMemberId(memberId.toString())
        assertThat(user).isNotNull
        assertThat(user?.simpleSignConnection?.personalNumber).isEqualTo(personalNumber)
        assertThat(user?.simpleSignConnection?.country).isEqualTo("SE")
    }

    @Test
    fun `exporting the same user twice simply ignored second attempt`() {
        val memberId = 123L
        val personalNumber = "201212121212"

        importer.on(
            MemberSignedWithoutBankId(memberId, personalNumber),
            Instant.now()
        )
        entityManager.clear()
        importer.on(
            MemberSignedWithoutBankId(memberId, personalNumber),
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
            MemberSignedWithoutBankId(memberId1, personalNumber),
            Instant.now()
        )
        entityManager.clear()
        importer.on(
            MemberSignedWithoutBankId(memberId2, personalNumber),
            Instant.now()
        )

        val users = userRepository.findAll()
        assertThat(users.size).isEqualTo(1)
        assertThat(users[0].associatedMemberId).isEqualTo(memberId2.toString())
        assertThat(users[0].simpleSignConnection?.personalNumber).isEqualTo(personalNumber)
    }

    @Test
    fun `a sign event that matches a Swedish login-created user should replace it`() {
        val memberId1 = 123L
        val memberId2 = 456L
        val personalNumber = "201212121212"

        val user = User(associatedMemberId = memberId1.toString())
        swedishBankIdCredentialRepository.saveAndFlush(
            SwedishBankIdCredential(user, personalNumber)
        )
        auditEventRepository.saveAndFlush(
            AuditEvent(user, AuditEvent.EventType.CREATED_ON_LOGIN)
        )
        entityManager.refresh(user)

        importer.on(
            MemberSignedWithoutBankId(memberId2, personalNumber),
            Instant.now()
        )

        val users = userRepository.findAll()
        assertThat(users.size).isEqualTo(1)
        assertThat(users[0].associatedMemberId).isEqualTo(memberId2.toString())
        assertThat(users[0].simpleSignConnection?.personalNumber).isEqualTo(personalNumber)
    }

    @Test
    fun `replace users only if it has the CREATED_ON_LOGIN event`() {
        val memberId1 = 123L
        val memberId2 = 456L
        val personalNumber = "201212121212"

        val user = User(associatedMemberId = memberId1.toString())
        swedishBankIdCredentialRepository.saveAndFlush(
            SwedishBankIdCredential(user, personalNumber)
        )
        entityManager.refresh(user)

        importer.on(
            MemberSignedWithoutBankId(memberId2, personalNumber),
            Instant.now()
        )

        val users = userRepository.findAll()
        assertThat(users.size).isEqualTo(1)
        assertThat(users[0].associatedMemberId).isEqualTo(memberId1.toString())
        assertThat(users[0].swedishBankIdCredential?.personalNumber).isEqualTo(personalNumber)
    }
}
