package com.hedvig.auth.models

import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager
import java.time.Instant

@DataJpaTest
class AuditEventTest {

    @Autowired
    lateinit var entityManager: TestEntityManager

    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var auditEventRepository: AuditEventRepository

    @Test
    fun `can create AuditEvent`() {
        val user = userRepository.saveAndFlush(User(associatedMemberId = "test"))
        val auditEvent = auditEventRepository.saveAndFlush(
            AuditEvent(
                user = user,
                eventType = AuditEvent.EventType.CREATED_ON_LOGIN
            )
        )
        assertThat(auditEvent.id).isNotEqualTo(0)
        assertThat(auditEvent.user).isEqualTo(user)
        assertThat(auditEvent.eventType).isEqualTo(AuditEvent.EventType.CREATED_ON_LOGIN)
        assertThat(auditEvent.timestamp).isBetween(
            Instant.now().minusSeconds(10),
            Instant.now().plusSeconds(10)
        )
    }

    @Test
    fun `should delete audit log when User is deleted`() {
        val user = userRepository.saveAndFlush(User(associatedMemberId = "test"))
        auditEventRepository.saveAndFlush(
            AuditEvent(
                user = user,
                eventType = AuditEvent.EventType.CREATED_ON_LOGIN
            )
        )
        entityManager.refresh(user)
        assertThat(auditEventRepository.count()).isEqualTo(1)
        userRepository.delete(user)
        assertThat(auditEventRepository.count()).isEqualTo(0)
    }

}
