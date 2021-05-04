package com.hedvig.memberservice.users

import com.hedvig.auth.services.UserService
import com.hedvig.memberservice.events.MemberSignedWithoutBankId
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
        export(
            memberId = e.memberId.toString(),
            personalNumber = e.nationalIdentification,
            countryCode = e.nationality.countryCode.alpha2
        )
    }

    @EventHandler
    @Transactional
    fun on(e: MemberSignedWithoutBankId) {
        export(
            memberId = e.memberId.toString(),
            personalNumber = e.ssn,
            countryCode = "SE"
        )
    }

    private fun export(
        memberId: String,
        personalNumber: String,
        countryCode: String
    ) {
        if (userService.findUserByAssociatedMemberId(memberId) != null) {
            logger.debug("Member ${memberId} was already has a user - skipping")
            return
        }

        val user = userService.findOrCreateUserWithCredential(
            UserService.Credential.SimpleSign(
                personalNumber = personalNumber,
                countryCode = countryCode
            ),
            UserService.Context(
                onboardingMemberId = memberId,
                authType = UserService.AuthType.SIGN
            )
        )

        if (user != null) {
            logger.debug("Created user ${user.id} for member $memberId with SimpleSign connection")
        } else {
            throw Exception("Failed to create user for member $memberId with SimpleSign connection")
        }
    }
}
