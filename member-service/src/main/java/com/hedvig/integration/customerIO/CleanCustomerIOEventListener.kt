package com.hedvig.integration.customerIO

import com.hedvig.external.event.ZignSecSignEvent
import com.hedvig.integration.notificationService.NotificationService
import com.hedvig.memberservice.events.DanishMemberSignedEvent
import com.hedvig.memberservice.events.MemberSignedEvent
import com.hedvig.memberservice.events.MemberSignedWithoutBankId
import com.hedvig.memberservice.events.MemberSimpleSignedEvent
import com.hedvig.memberservice.events.NorwegianMemberSignedEvent
import com.hedvig.memberservice.query.MemberRepository
import com.hedvig.memberservice.util.logger
import mu.KotlinLogging
import org.axonframework.config.ProcessingGroup
import org.axonframework.eventhandling.EventHandler
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

@Profile("customer.io")
@ConditionalOnProperty(value = ["customerio.siteId", "customerio.apiKey"], matchIfMissing = false)
@ProcessingGroup("CleanCustomerIO")
@Component
class CleanCustomerIOEventListener(
    private val memberRepository: MemberRepository,
    private val notificationService: NotificationService
) {
    @EventHandler
    fun on(event: MemberSignedEvent) {
        deleteUnsignedMembersWithSameInfoFromCustomerIo(event.id)
    }

    @EventHandler
    fun on(event: MemberSignedWithoutBankId) {
        deleteUnsignedMembersWithSameInfoFromCustomerIo(event.memberId)
    }

    @EventHandler
    fun on(event: MemberSimpleSignedEvent) {
        deleteUnsignedMembersWithSameInfoFromCustomerIo(event.memberId)
    }

    @EventHandler
    fun on(event: NorwegianMemberSignedEvent) {
        deleteUnsignedMembersWithSameInfoFromCustomerIo(event.memberId)
    }

    @EventHandler
    fun on(event: DanishMemberSignedEvent) {
        deleteUnsignedMembersWithSameInfoFromCustomerIo(event.memberId)
    }

    private fun deleteUnsignedMembersWithSameInfoFromCustomerIo(memberId: Long) {
        val memberMaybe = memberRepository.findById(memberId)
        if (!memberMaybe.isPresent) {
            logger.error("Cannot delete unsigned members with same info from customer.io since no member exist (memberId=${memberId})")
            return
        }
        val member = memberMaybe.get()
        if (member.email.isNullOrBlank()) {
            logger.error("Cannot delete unsigned members with same info from customer.io since email is empty (memberId=${memberId})")
            return
        }
        if (member.ssn.isNullOrBlank()) {
            logger.error("Cannot delete unsigned members with same info from customer.io since ssn is empty (memberId=${memberId})")
            return
        }
        val membersToDeleteFromCustomerIO = memberRepository.findNonSignedBySsnOrEmailAndNotId(
            ssn = member.ssn,
            email = member.email,
            memberId = member.id
        )
        membersToDeleteFromCustomerIO.forEach { memberToDelete ->
            try {
                notificationService.deleteCustomer(memberToDelete.id.toString())
                logger.debug("Deleted member=${memberToDelete.id} from customer.io since member=${member.id} signed")
            } catch (exception: Exception) {
                logger.error("Failed to delete member=${memberToDelete.id} from customer.io (exception=$exception)")
            }
            try {
                Thread.sleep(10)
            } catch (exception: Exception) {
                logger.error("Interrupted when throttling customer.io deletions")
            }
        }
    }
}
