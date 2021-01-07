package com.hedvig.memberservice.listeners

import com.hedvig.integration.productsPricing.CampaignService
import com.hedvig.memberservice.events.MemberCreatedEvent
import com.hedvig.memberservice.events.NameUpdatedEvent
import com.hedvig.memberservice.util.logger
import org.axonframework.config.ProcessingGroup
import org.axonframework.eventhandling.EventHandler
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
@ProcessingGroup("CreateCampaignOwner")
class CampaignOwnerUpdaterEventListener(
    val campaignService: CampaignService
) {

    @EventHandler
    fun on(event: MemberCreatedEvent) {
        logger.info("Handling event MemberCreatedEvent (memberId=${event.id})")
        try {
            campaignService.createCampaignOwnerMember(event.id)
        } catch (ex: RuntimeException) {
            logger.error(
                "Could not notify product-pricing about created campaign owner member for memberId: {}",
                event.id,
                ex
            )
        }
    }

    @EventHandler
    fun on(e: NameUpdatedEvent) {
        logger.debug("ON MEMBER NAME UPDATE EVENT FOR {}", e.memberId)
        try {
            e.firstName?.let {
                campaignService.memberNameUpdate(e.memberId, it)
            }
        } catch (ex: RuntimeException) {
            logger.error("Could not notify product-pricing about member name update for memberId: {}", e.memberId, ex)
        }
    }
}
