package com.hedvig.auth.import

import com.hedvig.auth.models.*
import com.hedvig.auth.models.AuditEvent
import com.hedvig.auth.models.SimpleSignConnection
import com.hedvig.auth.models.SimpleSignConnectionRepository
import com.hedvig.auth.models.UserRepository
import com.hedvig.memberservice.events.MemberSignedWithoutBankId
import java.time.Instant
import javax.transaction.Transactional
import org.axonframework.config.ProcessingGroup
import org.axonframework.eventhandling.EventHandler
import org.axonframework.eventhandling.Timestamp
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
@ProcessingGroup("SwedishRepairingSimpleSignMemberImporter")
class SwedishRepairingSimpleSignMemberImporter {

    @Autowired private lateinit var auditEventRepository: AuditEventRepository
    @Autowired private lateinit var userRepository: UserRepository
    @Autowired private lateinit var simpleSignConnectionRepository: SimpleSignConnectionRepository
    @Autowired private lateinit var swedishBankIdCredentialRepository: SwedishBankIdCredentialRepository

    private val logger = LoggerFactory.getLogger(javaClass)

    @EventHandler
    @Transactional
    fun on(e: MemberSignedWithoutBankId, @Timestamp timestamp: Instant) {
        if (userRepository.findByAssociatedMemberId(e.memberId.toString()) != null) {
            logger.info("Member ${e.memberId} was already imported - skipping")
            return
        }
        simpleSignConnectionRepository.findByPersonalNumberAndCountry(e.ssn, "SE")?.let { connection ->
            userRepository.delete(connection.user)
            userRepository.flush()
        }

        swedishBankIdCredentialRepository.findByPersonalNumber(e.ssn)?.let { credential ->
            val isCreatedThroughLogin = credential.user.auditLog.firstOrNull()?.eventType == AuditEvent.EventType.CREATED_ON_LOGIN
            if (isCreatedThroughLogin) {
                userRepository.delete(credential.user)
                userRepository.flush()
                logger.info("Deleted user ${credential.user.id} in favour of new correctly imported member ${e.memberId}")
            } else {
                logger.warn(
                    "Unexpectedly found existing Swedish user on MemberSignedWithoutBankId event: " +
                    "userMemberId = ${credential.user.associatedMemberId}, " +
                    "eventMemberId = ${e.memberId}"
                )
                return
            }
        }

        val user = User(associatedMemberId = e.memberId.toString(), createdAt = timestamp)
        user.simpleSignConnection = SimpleSignConnection(
            user = user,
            personalNumber = e.ssn,
            country = "SE",
            createdAt = timestamp
        )
        userRepository.saveAndFlush(user)
        auditEventRepository.save(
            AuditEvent(user = user, eventType = AuditEvent.EventType.CREATED_ON_IMPORT)
        )

        logger.info("Imported member ${e.memberId} as user ${user.id} with SimpleSign connection")
    }
}
