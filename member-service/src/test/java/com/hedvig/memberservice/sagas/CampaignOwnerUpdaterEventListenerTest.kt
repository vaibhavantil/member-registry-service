package com.hedvig.memberservice.sagas

import com.hedvig.integration.productsPricing.CampaignService
import com.hedvig.memberservice.aggregates.MemberStatus
import com.hedvig.memberservice.events.MemberCreatedEvent
import com.hedvig.memberservice.listeners.CampaignOwnerUpdaterEventListener
import io.mockk.MockKAnnotations
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import org.junit.Before
import org.junit.Test

class CampaignOwnerUpdaterEventListenerTest {
    @MockK(relaxed = true)
    lateinit var campaignService: CampaignService

    @Before
    fun setup() {
        MockKAnnotations.init(this)
    }

    @Test
    fun onMemberCreatedEvent_shouldCreateCampaignOwnerMember() {
        val eventHandler = CampaignOwnerUpdaterEventListener(campaignService)
        val event = MemberCreatedEvent(1337L, MemberStatus.INITIATED)
        eventHandler.on(event)
        verify(exactly = 1) { campaignService.createCampaignOwnerMember(1337L) }
    }
}
