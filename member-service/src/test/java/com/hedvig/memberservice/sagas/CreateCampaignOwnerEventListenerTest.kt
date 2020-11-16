package com.hedvig.memberservice.sagas

import com.hedvig.integration.productsPricing.ProductApi
import com.hedvig.memberservice.aggregates.MemberStatus
import com.hedvig.memberservice.events.MemberCreatedEvent
import com.hedvig.memberservice.listeners.CreateCampaignOwnerEventListener
import io.mockk.MockKAnnotations
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import org.junit.Before
import org.junit.Test

class CreateCampaignOwnerEventListenerTest {
    @MockK(relaxed = true)
    lateinit var productApi: ProductApi

    @Before
    fun setup() {
        MockKAnnotations.init(this)
    }

    @Test
    fun onMemberCreatedEvent_shouldCreateCampaignOwnerMember() {
        val eventHandler = CreateCampaignOwnerEventListener(productApi)
        val event = MemberCreatedEvent(1337L, MemberStatus.INITIATED)
        eventHandler.on(event)
        verify(exactly = 1) { productApi.createCampaignOwnerMember(1337L) }
    }
}
