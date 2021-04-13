package com.hedvig.auth.import

import com.hedvig.auth.model.SwedishBankIdCredential
import com.hedvig.auth.model.SwedishBankIdCredentialRepository
import com.hedvig.auth.model.User
import com.hedvig.auth.model.UserRepository
import com.hedvig.memberservice.events.MemberSignedEvent
import javax.transaction.Transactional
import org.axonframework.config.ProcessingGroup
import org.axonframework.eventhandling.EventHandler
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
@ProcessingGroup("SwedishMemberImporter")
class SwedishMemberImporter(
    private val userRepository: UserRepository,
    private val swedishBankIdCredentialRepository: SwedishBankIdCredentialRepository
) {

    private val logger = LoggerFactory.getLogger(javaClass)

    @EventHandler
    @Transactional
    fun on(e: MemberSignedEvent) {
        if (userRepository.findByAssociatedMemberId(e.id.toString()) != null) {
            logger.info("Member ${e.id} was already imported - skipping")
            return
        }

        val personalNumber = e.ssn ?: run {
            logger.info("Member ${e.id} was signed without personal number - skipping")
            return
        }
        swedishBankIdCredentialRepository.findByPersonalNumber(e.ssn)?.let { credential ->
            userRepository.delete(credential.user)
            userRepository.flush()
        }

        val memberId = e.id.toString()
        val user = User(associatedMemberId = memberId)
        user.swedishBankIdCredential = SwedishBankIdCredential(user, personalNumber)
        userRepository.save(user)

        logger.info("Imported member $memberId as user ${user.id} with Swedish BankID credentials")
    }
}
