package com.hedvig.auth.import

import com.hedvig.auth.models.AuditEvent
import com.hedvig.auth.models.AuditEventRepository
import com.hedvig.auth.models.SwedishBankIdCredential
import com.hedvig.auth.models.SwedishBankIdCredentialRepository
import com.hedvig.auth.models.User
import com.hedvig.auth.models.UserRepository
import com.hedvig.memberservice.events.MemberSignedEvent
import com.hedvig.memberservice.events.MemberSignedWithoutBankId
import com.hedvig.memberservice.query.SignedMemberEntity
import com.hedvig.memberservice.query.SignedMemberRepository
import java.time.Instant
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager
import org.springframework.context.ApplicationContext

@DataJpaTest
internal class OldLoyalSwedishMemberRepairingImporterTest @Autowired constructor(
    private val userRepository: UserRepository,
    private val auditEventRepository: AuditEventRepository,
    private val signedMemberRepository: SignedMemberRepository,
    private val entityManager: TestEntityManager
) {

    private lateinit var importer: OldLoyalSwedishMemberRepairingImporter

    @BeforeEach
    fun setup(@Autowired context: ApplicationContext) {
        importer = context.autowireCapableBeanFactory.createBean(OldLoyalSwedishMemberRepairingImporter::class.java)
    }

    @Test
    fun `signed member is imported on event`() {
        val memberId = 123L
        val personalNumber = "201212121212"

        signedMemberRepository.saveAndFlush(
            SignedMemberEntity().also {
                it.id = 123L
                it.ssn = personalNumber
            }
        )

        importer.on(
            MemberSignedEvent(memberId, "ref", "sig", "oscp", null),
            Instant.now()
        )

        val user = userRepository.findByAssociatedMemberId(memberId.toString())
        assertThat(user).isNotNull
        assertThat(user?.swedishBankIdCredential?.personalNumber).isEqualTo(personalNumber)
    }

    @Test
    fun `signed member is not imported on event if it has SSN`() {
        val memberId = 123L
        val personalNumber = "201212121212"

        signedMemberRepository.saveAndFlush(
            SignedMemberEntity().also {
                it.id = 123L
                it.ssn = personalNumber
            }
        )

        importer.on(
            MemberSignedEvent(memberId, "ref", "sig", "oscp", personalNumber),
            Instant.now()
        )

        val user = userRepository.findByAssociatedMemberId(memberId.toString())
        assertThat(user).isNull()
    }

    @Test
    fun `exporting the same user twice simply ignored second attempt`() {
        val memberId = 123L
        val personalNumber = "201212121212"

        signedMemberRepository.saveAndFlush(
            SignedMemberEntity().also {
                it.id = 123L
                it.ssn = personalNumber
            }
        )

        importer.on(
            MemberSignedEvent(memberId, "ref", "sig", "oscp", null),
            Instant.now()
        )
        entityManager.clear()
        importer.on(
            MemberSignedEvent(memberId, "ref", "sig", "oscp", null),
            Instant.now()
        )

        val users = userRepository.findAll()
        assertThat(users.size).isEqualTo(1)
    }

    @Test
    fun `member signing with same personal number should replace old one if it was created on login`() {
        val memberId1 = 123L
        val memberId2 = 456L
        val personalNumber = "201212121212"

        signedMemberRepository.saveAndFlush(
            SignedMemberEntity().also {
                it.id = memberId2
                it.ssn = personalNumber
            }
        )
        val user = userRepository.saveAndFlush(
            User(associatedMemberId = memberId1.toString()).also {
                it.swedishBankIdCredential = SwedishBankIdCredential(it, personalNumber)
            }
        )
        auditEventRepository.saveAndFlush(
            AuditEvent(user, AuditEvent.EventType.CREATED_ON_LOGIN)
        )
        entityManager.refresh(user)

        importer.on(
            MemberSignedEvent(memberId2, "ref", "sig", "oscp", null),
            Instant.now()
        )

        val users = userRepository.findAll()
        assertThat(users.size).isEqualTo(1)
        assertThat(users[0].associatedMemberId).isEqualTo(memberId2.toString())
        assertThat(users[0].swedishBankIdCredential?.personalNumber).isEqualTo(personalNumber)
    }

    @Test
    fun `replace users only if it has the CREATED_ON_LOGIN event`() {
        val memberId1 = 123L
        val memberId2 = 456L
        val personalNumber = "201212121212"

        signedMemberRepository.saveAndFlush(
            SignedMemberEntity().also {
                it.id = memberId2
                it.ssn = personalNumber
            }
        )
        val user = userRepository.saveAndFlush(
            User(associatedMemberId = memberId1.toString()).also {
                it.swedishBankIdCredential = SwedishBankIdCredential(it, personalNumber)
            }
        )
        auditEventRepository.saveAndFlush(
            AuditEvent(user, AuditEvent.EventType.CREATED_ON_IMPORT)
        )

        importer.on(
            MemberSignedEvent(memberId2, "ref", "sig", "oscp", null),
            Instant.now()
        )

        val users = userRepository.findAll()
        assertThat(users.size).isEqualTo(1)
        assertThat(users[0].associatedMemberId).isEqualTo(memberId1.toString())
        assertThat(users[0].swedishBankIdCredential?.personalNumber).isEqualTo(personalNumber)
    }
}
