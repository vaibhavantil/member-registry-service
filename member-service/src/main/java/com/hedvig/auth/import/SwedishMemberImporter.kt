package com.hedvig.auth.import

import com.hedvig.auth.models.*
import com.hedvig.auth.models.AuditEvent
import com.hedvig.auth.models.SwedishBankIdCredential
import com.hedvig.auth.models.SwedishBankIdCredentialRepository
import com.hedvig.auth.models.UserRepository
import com.hedvig.memberservice.events.MemberSignedEvent
import java.time.Instant
import javax.transaction.Transactional
import org.axonframework.config.ProcessingGroup
import org.axonframework.eventhandling.EventHandler
import org.axonframework.eventhandling.Timestamp
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
@ProcessingGroup("SwedishMemberImporter")
class SwedishMemberImporter {

    @Autowired private lateinit var auditEventRepository: AuditEventRepository
    @Autowired private lateinit var userRepository: UserRepository
    @Autowired private lateinit var swedishBankIdCredentialRepository: SwedishBankIdCredentialRepository

    private val logger = LoggerFactory.getLogger(javaClass)

    @EventHandler
    @Transactional
    fun on(e: MemberSignedEvent, @Timestamp timestamp: Instant) {
        if (userRepository.findByAssociatedMemberId(e.id.toString()) != null) {
            logger.info("Member ${e.id} was already imported - skipping")
            return
        }

        val personalNumber = e.ssn ?: run {
            logger.info("Member ${e.id} was signed without personal number - skipping")
            return
        }
        swedishBankIdCredentialRepository.findByPersonalNumber(e.ssn)?.let { credential ->
            userRepository.delete(credential.user)
            userRepository.flush()
        }

        val memberId = e.id.toString()
        val user = User(associatedMemberId = memberId, createdAt = timestamp)
        user.swedishBankIdCredential = SwedishBankIdCredential(
            user = user,
            personalNumber = personalNumber,
            createdAt = timestamp
        )
        userRepository.saveAndFlush(user)

        auditEventRepository.save(
            AuditEvent(
                user = user,
                eventType = AuditEvent.EventType.CREATED_ON_IMPORT
            )
        )

        logger.info("Imported member $memberId as user ${user.id} with Swedish BankID credentials")
    }
}
