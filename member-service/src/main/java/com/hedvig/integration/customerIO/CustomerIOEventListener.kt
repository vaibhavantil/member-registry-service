package com.hedvig.integration.customerIO

import com.hedvig.memberservice.events.MemberSignedEvent
import com.hedvig.memberservice.query.MemberRepository
import mu.KotlinLogging
import org.axonframework.config.ProcessingGroup
import org.axonframework.eventhandling.EventHandler
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

@Profile("customer.io")
@ConditionalOnProperty(value = ["customerio.siteId", "customerio.apiKey"], matchIfMissing = false)
@ProcessingGroup("CustomerIO")
@Component
class CustomerIOEventListener(
    private val memberRepository: MemberRepository,
    private val customerIO: CustomerIO
) {
    private val logger = KotlinLogging.logger {}

    @EventHandler
    fun on(event: MemberSignedEvent) {
        val member = memberRepository.findById(event.id).get()
        val membersToDeleteFromCustomerIO = memberRepository.findNonSignedBySsnOrEmailAndNotId(
            ssn = member.ssn,
            email = member.email,
            memberId = member.id
        )
        membersToDeleteFromCustomerIO.forEach { memberToRemove ->
            try {
                customerIO.deleteUser(userId = memberToRemove.id.toString())
                logger.info { "Deleted member=${memberToRemove.id} from customer.io since member=${member.id} signed" }
            } catch (exception: Exception) {
                logger.error { "Failed to delete member=${memberToRemove.id} from customer.io (exception=$exception)" }
            }
            try {
                Thread.sleep(10)
            } catch (exception: Exception) {
                logger.error { "Interrupted when throttling customer.io deletions" }
            }
        }
    }
}
