package com.hedvig.auth.import

import com.fasterxml.jackson.databind.ObjectMapper
import com.hedvig.auth.model.User
import com.hedvig.auth.model.UserRepository
import com.hedvig.auth.model.ZignSecCredential
import com.hedvig.auth.model.ZignSecCredentialRepository
import com.hedvig.external.zignSec.client.dto.ZignSecNotificationRequest
import com.hedvig.memberservice.events.DanishMemberSignedEvent
import com.hedvig.memberservice.events.NorwegianMemberSignedEvent
import javax.transaction.Transactional
import org.axonframework.config.ProcessingGroup
import org.axonframework.eventhandling.EventHandler
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
    fun on(e: NorwegianMemberSignedEvent) {
        importZignSecAuthenticatedMember(
            memberId = e.memberId,
            zignSecNotification = decodeZignSecNotification(e.providerJsonResponse)
        )
    }

    @EventHandler
    @Transactional
    fun on(e: DanishMemberSignedEvent) {
        importZignSecAuthenticatedMember(
            memberId = e.memberId,
            zignSecNotification = decodeZignSecNotification(e.providerJsonResponse)
        )
    }

    private fun importZignSecAuthenticatedMember(memberId: Long, zignSecNotification: ZignSecNotificationRequest) {
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

        val user = User(associatedMemberId = memberId.toString())
        user.zignSecCredential = ZignSecCredential(user, idProviderName, idProviderPersonId)
        userRepository.save(user)

        logger.info("Imported member $memberId as user ${user.id} with ZignSec credentials")
    }

    private fun decodeZignSecNotification(json: String): ZignSecNotificationRequest {
        return objectMapper.readValue(json, ZignSecNotificationRequest::class.java)
    }
}
