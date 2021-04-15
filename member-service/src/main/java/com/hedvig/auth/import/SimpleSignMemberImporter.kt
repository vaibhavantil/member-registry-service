package com.hedvig.auth.import

import com.hedvig.auth.models.*
import com.hedvig.auth.models.AuditEvent
import com.hedvig.auth.models.SimpleSignConnection
import com.hedvig.auth.models.SimpleSignConnectionRepository
import com.hedvig.auth.models.UserRepository
import com.hedvig.memberservice.events.MemberSimpleSignedEvent
import java.time.Instant
import javax.transaction.Transactional
import org.axonframework.config.ProcessingGroup
import org.axonframework.eventhandling.EventHandler
import org.axonframework.eventhandling.Timestamp
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
@ProcessingGroup("SimpleSignMemberImporter")
class SimpleSignMemberImporter {

    @Autowired private lateinit var auditEventRepository: AuditEventRepository
    @Autowired private lateinit var userRepository: UserRepository
    @Autowired private lateinit var simpleSignConnectionRepository: SimpleSignConnectionRepository

    private val logger = LoggerFactory.getLogger(javaClass)

    @EventHandler
    @Transactional
    fun on(e: MemberSimpleSignedEvent, @Timestamp timestamp: Instant) {
        if (userRepository.findByAssociatedMemberId(e.memberId.toString()) != null) {
            logger.info("Member ${e.memberId} was already imported - skipping")
            return
        }

        val personalNumber = e.nationalIdentification
        val country = when (e.nationality) {
            MemberSimpleSignedEvent.Nationality.SWEDEN -> "SE"
            MemberSimpleSignedEvent.Nationality.NORWAY -> "NO"
            MemberSimpleSignedEvent.Nationality.DENMARK -> "DK"
        }

        simpleSignConnectionRepository.findByPersonalNumberAndCountry(personalNumber, country)?.let { connection ->
            userRepository.delete(connection.user)
            userRepository.flush()
        }

        val user = User(
            associatedMemberId = e.memberId.toString(),
            createdAt = timestamp
        )
        user.simpleSignConnection = SimpleSignConnection(
            user = user,
            personalNumber = personalNumber,
            country = country,
            createdAt = timestamp
        )
        userRepository.saveAndFlush(user)

        auditEventRepository.save(
            AuditEvent(
                user = user,
                eventType = AuditEvent.EventType.CREATED_ON_IMPORT
            )
        )

        logger.info("Imported member ${e.memberId} as user ${user.id} with SimpleSign connection")
    }
}
