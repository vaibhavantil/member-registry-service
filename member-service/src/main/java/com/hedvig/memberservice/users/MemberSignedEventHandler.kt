package com.hedvig.memberservice.users

import com.hedvig.auth.services.UserService
import com.hedvig.memberservice.events.MemberSignedEvent
import javax.transaction.Transactional
import org.axonframework.eventhandling.EventHandler
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class MemberSignedEventHandler {

    @Autowired private lateinit var userService: UserService

    private val logger = LoggerFactory.getLogger(javaClass)

    @EventHandler
    @Transactional
    fun on(e: MemberSignedEvent) {
        val memberId = e.id.toString()

        if (userService.findUserByAssociatedMemberId(memberId) != null) {
            logger.info("Member ${e.id} was already has a user - skipping")
            return
        }

        val personalNumber = e.ssn ?: run {
            logger.info("Member ${e.id} was signed without personal number - skipping")
            return
        }

        val user = userService.findOrCreateUserWithCredential(
            UserService.Credential.SwedishBankID(
                personalNumber = personalNumber
            ),
            UserService.Context(
                onboardingMemberId = memberId,
                authType = UserService.AuthType.SIGN
            )
        )

        if (user != null) {
            logger.info("Created user ${user.id} for member $memberId with  Swedish BankID credential")
        } else {
            throw Exception("Failed to create user for member $memberId with Swedish BankID credential")
        }
    }
}
