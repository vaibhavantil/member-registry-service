package com.hedvig.auth.import

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.hedvig.auth.models.*
import com.hedvig.auth.models.AuditEvent
import com.hedvig.auth.models.UserRepository
import com.hedvig.auth.models.ZignSecCredential
import com.hedvig.auth.models.ZignSecCredentialRepository
import com.hedvig.external.zignSec.client.dto.ZignSecNotificationRequest
import com.hedvig.memberservice.events.DanishMemberSignedEvent
import com.hedvig.memberservice.events.NorwegianMemberSignedEvent
import java.time.Instant
import javax.transaction.Transactional
import org.axonframework.config.ProcessingGroup
import org.axonframework.eventhandling.EventHandler
import org.axonframework.eventhandling.Timestamp
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
@ProcessingGroup("ZignSecMemberImporter")
class ZignSecMemberImporter {

    @Autowired private lateinit var auditEventRepository: AuditEventRepository
    @Autowired private lateinit var userRepository: UserRepository
    @Autowired private lateinit var zignSecCredentialRepository: ZignSecCredentialRepository

    private val objectMapper: ObjectMapper = ObjectMapper()
        .registerKotlinModule()
        .registerModule(JavaTimeModule())

    private val logger = LoggerFactory.getLogger(javaClass)

    @EventHandler
    @Transactional
    fun on(e: NorwegianMemberSignedEvent, @Timestamp timestamp: Instant) {
        importZignSecAuthenticatedMember(
            memberId = e.memberId,
            zignSecNotification = decodeZignSecNotification(e.providerJsonResponse),
            timestamp = timestamp
        )
    }

    @EventHandler
    @Transactional
    fun on(e: DanishMemberSignedEvent, @Timestamp timestamp: Instant) {
        importZignSecAuthenticatedMember(
            memberId = e.memberId,
            zignSecNotification = decodeZignSecNotification(e.providerJsonResponse),
            timestamp = timestamp
        )
    }

    private fun importZignSecAuthenticatedMember(
        memberId: Long,
        zignSecNotification: ZignSecNotificationRequest,
        timestamp: Instant
    ) {
        if (userRepository.findByAssociatedMemberId(memberId.toString()) != null) {
            logger.info("Member $memberId was already imported - skipping")
            return
        }

        val idProviderName = zignSecNotification.identity?.idProviderName ?: run {
            logger.warn("Member $memberId is missing idProviderName - skipping")
            return
        }
        val idProviderPersonId = zignSecNotification.identity?.idProviderPersonId ?: run {
            logger.warn("Member $memberId is missing idProviderPersonId - skipping")
            return
        }

        zignSecCredentialRepository.findByIdProviderNameAndIdProviderPersonId(idProviderName, idProviderPersonId)?.let { credential ->
            userRepository.delete(credential.user)
            userRepository.flush()
        }

        val user = User(associatedMemberId = memberId.toString(), createdAt = timestamp)
        user.zignSecCredential = ZignSecCredential(
            user = user,
            idProviderName = idProviderName,
            idProviderPersonId = idProviderPersonId,
            createdAt = timestamp
        )
        userRepository.saveAndFlush(user)

        auditEventRepository.save(
            AuditEvent(
                user = user,
                eventType = AuditEvent.EventType.CREATED_ON_IMPORT
            )
        )

        logger.info("Imported member $memberId as user ${user.id} with ZignSec credentials")
    }

    private fun decodeZignSecNotification(json: String): ZignSecNotificationRequest {
        return objectMapper.readValue(json, ZignSecNotificationRequest::class.java)
    }
}
