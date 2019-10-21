package com.hedvig.integration.customerIO

import com.hedvig.memberservice.aggregates.MemberStatus
import com.hedvig.memberservice.events.MemberSignedEvent
import com.hedvig.memberservice.query.MemberEntity
import com.hedvig.memberservice.query.MemberRepository
import mu.KotlinLogging
import org.axonframework.config.ProcessingGroup
import org.axonframework.eventhandling.EventHandler
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile

@Profile("customer.io")
@ProcessingGroup("CustomerIO")
class CustomerIOEventListener @Autowired constructor(
    private val memberRepository: MemberRepository,
    private val customerIO: CustomerIO
){
    private val logger = KotlinLogging.logger {}

    @EventHandler
    fun on(event: MemberSignedEvent) {
        val member = memberRepository.findById(event.id).get()
        val membersToRemoveFromCustomerIO = getNonSignedMembersWithSameSsnOrEmail(
            memberId = member.id,
            ssn = member.ssn,
            email = member.email
        )
        membersToRemoveFromCustomerIO.forEach { memberToRemove ->
            try {
                customerIO.deleteCustomerIOUser(userId = memberToRemove.id.toString())
                logger.info { "Deleted member=${memberToRemove.id} from customer.io" }
            } catch (exception: Exception) {
                logger.info { "Failed to delete member=${memberToRemove.id} from customer.io" }
            }
        }
    }

    private fun getNonSignedMembersWithSameSsnOrEmail(memberId: Long, ssn: String, email: String): List<MemberEntity> =
        memberRepository.findBySsnOrEmail(ssn, email)
            .filter { it.id != memberId } // To avoid race condition
            .filter { it.status != MemberStatus.SIGNED }
}
