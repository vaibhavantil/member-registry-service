package com.hedvig.memberservice.listeners

import com.hedvig.integration.productsPricing.ProductApi
import com.hedvig.memberservice.events.MemberCreatedEvent
import org.axonframework.config.ProcessingGroup
import org.axonframework.eventhandling.EventHandler
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
@ProcessingGroup("CreateCampaignOwner")
class CreateCampaignOwnerEventListener(
    val productApi: ProductApi
) {

    @EventHandler
    fun on(event: MemberCreatedEvent) {
        logger.info("Handling event MemberCreatedEvent (memberId=${event.id})")
        try {
            productApi.createCampaignOwnerMember(event.id)
        } catch (ex: RuntimeException) {
            logger.error(
                "Could not notify product-pricing about created campaign owner member for memberId: {}",
                event.id,
                ex
            )
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(CreateCampaignOwnerEventListener::class.java)
    }
}
