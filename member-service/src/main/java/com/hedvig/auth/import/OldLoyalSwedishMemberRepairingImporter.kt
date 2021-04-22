package com.hedvig.auth.import

import com.hedvig.auth.models.*
import com.hedvig.auth.models.AuditEvent
import com.hedvig.auth.models.SimpleSignConnection
import com.hedvig.auth.models.SimpleSignConnectionRepository
import com.hedvig.auth.models.UserRepository
import com.hedvig.memberservice.events.MemberSignedEvent
import com.hedvig.memberservice.events.MemberSignedWithoutBankId
import com.hedvig.memberservice.query.SignedMemberRepository
import java.time.Instant
import javax.transaction.Transactional
import org.axonframework.config.ProcessingGroup
import org.axonframework.eventhandling.EventHandler
import org.axonframework.eventhandling.Timestamp
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.data.ConditionalOnRepositoryType
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Component

/**
 * This migration targets members that were signed without an SSN value.
 * This is the case for members who signed before 2018-10-09, where the SSN value on said
 * event was introduced.
 *
 * In order to solve this, we must create them by stitching together some other concepts,
 * and delete any users that were created upon login because their real ones were missing.
 */
@Component
@ProcessingGroup("OldLoyalSwedishMemberRepairingImporter")
class OldLoyalSwedishMemberRepairingImporter {

    @Autowired private lateinit var auditEventRepository: AuditEventRepository
    @Autowired private lateinit var userRepository: UserRepository
    @Autowired private lateinit var swedishBankIdCredentialRepository: SwedishBankIdCredentialRepository
    @Autowired private lateinit var signedMemberRepository: SignedMemberRepository

    private val logger = LoggerFactory.getLogger(javaClass)

    @EventHandler
    @Transactional
    fun on(e: MemberSignedEvent, @Timestamp timestamp: Instant) {
        if (e.ssn != null) return

        if (userRepository.findByAssociatedMemberId(e.id.toString()) != null) {
            logger.info("Member ${e.id} was already imported - skipping")
            return
        }

        val personalNumber = signedMemberRepository.findByIdOrNull(e.id)?.ssn ?: return

        swedishBankIdCredentialRepository.findByPersonalNumber(personalNumber)?.let { credential ->
            val isCreatedThroughLogin = credential.user.auditLog.firstOrNull()?.eventType == AuditEvent.EventType.CREATED_ON_LOGIN
            if (isCreatedThroughLogin) {
                userRepository.delete(credential.user)
                userRepository.flush()
                logger.info(
                    "Deleted user ${credential.user.id} with memberId ${credential.user.associatedMemberId} " +
                    "in favour of new correctly imported member ${e.id}"
                )
            } else {
                return
            }
        }

        val user = User(associatedMemberId = e.id.toString(), createdAt = timestamp)
        user.swedishBankIdCredential = SwedishBankIdCredential(
            user = user,
            personalNumber = personalNumber,
            createdAt = timestamp
        )
        userRepository.saveAndFlush(user)

        auditEventRepository.save(
            AuditEvent(user, AuditEvent.EventType.CREATED_ON_IMPORT)
        )

        logger.info("Imported member ${e.id} as user ${user.id} with SwedishBankID credentials")
    }
}
