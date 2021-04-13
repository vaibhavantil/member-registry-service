package com.hedvig.auth.import

import com.fasterxml.jackson.databind.ObjectMapper
import com.hedvig.auth.model.User
import com.hedvig.auth.model.UserRepository
import com.hedvig.auth.model.ZignSecCredential
import com.hedvig.auth.model.ZignSecCredentialRepository
import com.hedvig.external.zignSec.client.dto.ZignSecNotificationRequest
import com.hedvig.memberservice.events.DanishMemberSignedEvent
import com.hedvig.memberservice.events.NorwegianMemberSignedEvent
import java.time.Instant
import javax.transaction.Transactional
import org.axonframework.config.ProcessingGroup
import org.axonframework.eventhandling.EventHandler
import org.axonframework.eventhandling.Timestamp
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
@ProcessingGroup("ZignSecMemberImporter")
class ZignSecMemberImporter(
    private val userRepository: UserRepository,
    private val zignSecCredentialRepository: ZignSecCredentialRepository,
    private val objectMapper: ObjectMapper
) {

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
        userRepository.save(user)

        logger.info("Imported member $memberId as user ${user.id} with ZignSec credentials")
    }

    private fun decodeZignSecNotification(json: String): ZignSecNotificationRequest {
        return objectMapper.readValue(json, ZignSecNotificationRequest::class.java)
    }
}
