package com.hedvig.integration.productsPricing

import com.hedvig.integration.productsPricing.dto.MemberCreatedRequest
import com.hedvig.integration.productsPricing.dto.MemberNameUpdateRequest
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod

@FeignClient(name = "productPricing", url = "\${hedvig.productsPricing.url:product-pricing}")
interface ProductPricingClient {
    @PostMapping("/_/campaign/member/create")
    fun createCampaignOwnerMember(@RequestBody req: MemberCreatedRequest): ResponseEntity<Void>

    @PostMapping("/_/campaign/member/update/name")
    fun updateCampaignMemberName(@RequestBody req: MemberNameUpdateRequest): ResponseEntity<Void>

    @GetMapping("/_/contracts/members/{memberId}/hasContract")
    fun hasContract(@PathVariable("memberId") memberId: Long): ResponseEntity<Boolean>
}
