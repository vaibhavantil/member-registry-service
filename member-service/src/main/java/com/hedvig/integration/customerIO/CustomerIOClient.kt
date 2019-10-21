package com.hedvig.integration.customerIO

import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PathVariable

@FeignClient(
    name = "customer.io.client",
    url = "\${customerio.url:https://track.customer.io/api}",
    configuration = [CustomerIOFeignConfiguration::class]
)
interface CustomerIOClient {
    @DeleteMapping("/v1/customers/{userId}")
    fun delete(@PathVariable userId: String)
}
