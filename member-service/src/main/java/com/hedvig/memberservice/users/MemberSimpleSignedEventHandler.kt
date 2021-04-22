package com.hedvig.memberservice.users

import com.hedvig.auth.services.UserService
import com.hedvig.memberservice.events.MemberSimpleSignedEvent
import javax.transaction.Transactional
import org.axonframework.eventhandling.EventHandler
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class MemberSimpleSignedEventHandler {

    @Autowired lateinit var userService: UserService

    private val logger = LoggerFactory.getLogger(javaClass)

    @EventHandler
    @Transactional
    fun on(e: MemberSimpleSignedEvent) {
        val memberId = e.memberId.toString()

        if (userService.findUserByAssociatedMemberId(memberId) != null) {
            logger.info("Member ${memberId} was already has a user - skipping")
            return
        }

        val personalNumber = e.nationalIdentification
        val country = when (e.nationality) {
            MemberSimpleSignedEvent.Nationality.SWEDEN -> "SE"
            MemberSimpleSignedEvent.Nationality.NORWAY -> "NO"
            MemberSimpleSignedEvent.Nationality.DENMARK -> "DK"
        }

        val user = userService.findOrCreateUserWithCredential(
            UserService.Credential.SimpleSign(
                personalNumber = personalNumber,
                countryCode = country
            ),
            UserService.Context(
                onboardingMemberId = memberId,
                authType = UserService.AuthType.SIGN
            )
        )

        if (user != null) {
            logger.info("Created user ${user.id} for member $memberId with SimpleSign connection")
        } else {
            throw Exception("Failed to create user for member $memberId with SimpleSign connection")
        }
    }
}
