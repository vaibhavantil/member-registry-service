package com.hedvig.memberservice.users

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.hedvig.auth.models.MemberId
import com.hedvig.auth.services.UserService
import com.hedvig.external.zignSec.client.dto.ZignSecNotificationRequest
import com.hedvig.memberservice.events.NorwegianMemberSignedEvent
import javax.transaction.Transactional
import org.axonframework.eventhandling.EventHandler
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class NorwegianMemberSignedEventHandler {

    @Autowired private lateinit var userService: UserService

    private val objectMapper: ObjectMapper = ObjectMapper()
        .registerKotlinModule()
        .registerModule(JavaTimeModule())

    private val logger = LoggerFactory.getLogger(javaClass)

    @EventHandler
    @Transactional
    fun on(e: NorwegianMemberSignedEvent) {
        importZignSecAuthenticatedMember(
            memberId = e.memberId.toString(),
            zignSecNotification = decodeZignSecNotification(e.providerJsonResponse)
        )
    }

    private fun importZignSecAuthenticatedMember(
        memberId: MemberId,
        zignSecNotification: ZignSecNotificationRequest
    ) {
        if (userService.findUserByAssociatedMemberId(memberId) != null) {
            logger.debug("Member $memberId already has a user - skipping")
            return
        }

        val idProviderName = zignSecNotification.identity?.idProviderName ?: run {
            logger.debug("Member $memberId is missing idProviderName - skipping")
            return
        }
        val idProviderPersonId = zignSecNotification.identity?.idProviderPersonId ?: run {
            logger.debug("Member $memberId is missing idProviderPersonId - skipping")
            return
        }

        val user = userService.findOrCreateUserWithCredential(
            UserService.Credential.ZignSec(
                idProviderName = idProviderName,
                idProviderPersonId = idProviderPersonId
            ),
            UserService.Context(
                onboardingMemberId = memberId,
                authType = UserService.AuthType.SIGN
            )
        )

        if (user != null) {
            logger.debug("Created user ${user.id} for member $memberId with ZignSec credential")
        } else {
            throw Exception("Failed to create user for member $memberId with ZignSec credential")
        }
    }

    private fun decodeZignSecNotification(json: String): ZignSecNotificationRequest {
        return objectMapper.readValue(json, ZignSecNotificationRequest::class.java)
    }
}
