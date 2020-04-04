package com.hedvig.integration.apigateway

import com.hedvig.config.FeignConfig
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod

@FeignClient(name = "api-gateway", url = "\${hedvig.api-gateway.url:api-gateway}", configuration = [FeignConfig::class])
interface ApiGatewayClient {
    @RequestMapping(value = ["/_/reassign"], method = [RequestMethod.POST])
    fun reassignMember(@RequestHeader token: String, @RequestBody dto: ReassignMemberDto)
}
