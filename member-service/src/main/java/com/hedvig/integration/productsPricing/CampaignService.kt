package com.hedvig.integration.productsPricing

import com.hedvig.integration.productsPricing.dto.MemberCreatedRequest
import com.hedvig.integration.productsPricing.dto.MemberNameUpdateRequest
import org.springframework.stereotype.Service

@Service
class CampaignService(
    private val client: ProductPricingClient
) {

    fun createCampaignOwnerMember(memberId: Long) =
        client.createCampaignOwnerMember(MemberCreatedRequest(memberId.toString()))

    fun memberNameUpdate(memberId: Long, name: String?) =
        client.updateCampaignMemberName(MemberNameUpdateRequest(memberId.toString(), name))

}
