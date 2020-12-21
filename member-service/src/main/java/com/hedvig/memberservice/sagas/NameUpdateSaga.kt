package com.hedvig.memberservice.sagas

import com.hedvig.integration.productsPricing.CampaignService
import com.hedvig.memberservice.events.NameUpdatedEvent
import org.axonframework.eventhandling.saga.EndSaga
import org.axonframework.eventhandling.saga.SagaEventHandler
import org.axonframework.eventhandling.saga.StartSaga
import org.axonframework.spring.stereotype.Saga
import org.slf4j.LoggerFactory

@Saga(configurationBean = "memberNameUpdateSagaConfiguration")
class NameUpdateSaga(
    val campaignService: CampaignService
) {

    @SagaEventHandler(associationProperty = "memberId")
    @StartSaga
    @EndSaga
    fun onMemberNameUpdate(e: NameUpdatedEvent) {
        log.debug("ON MEMBER NAME UPDATE EVENT FOR {}", e.memberId)
        try {
            e.firstName?.let {
                campaignService.memberNameUpdate(e.memberId, it)
            }
        } catch (ex: RuntimeException) {
            log.error("Could not notify product-pricing about member name update for memberId: {}", e.memberId, ex)
        }
    }

    companion object {
        private val log = LoggerFactory.getLogger(NameUpdateSaga::class.java)
    }
}
