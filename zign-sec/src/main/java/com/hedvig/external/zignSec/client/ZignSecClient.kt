package com.hedvig.external.zignSec.client

import com.hedvig.external.zignSec.client.dto.ZignSecRequestBody
import com.hedvig.external.zignSec.client.dto.ZignSecResponse
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader

@FeignClient(name = "bankId", url = "\${hedvig.external.zignSec.eid.baseurl:https://test.zignsec.com/v2/eid}")
interface ZignSecClient {

    @PostMapping(value = ["/{bankid_selector}"], consumes = ["application/json"])
    fun auth(
        @PathVariable("bankid_selector") bankIdSelector: String,
        @RequestHeader("Authorization") authorization: String,
        @RequestHeader("Host") host: String,
        @RequestBody body: ZignSecRequestBody
    ): ZignSecResponse
}

