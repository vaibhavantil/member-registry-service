package com.hedvig.external.zignSec.client

import com.hedvig.external.config.ZignSecFeignConfig
import com.hedvig.external.zignSec.client.dto.ZignSecCollectResponse
import com.hedvig.external.zignSec.client.dto.ZignSecRequestBody
import com.hedvig.external.zignSec.client.dto.ZignSecResponse
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import java.util.*

@FeignClient(name = "zignSecClient", url = "\${hedvig.external.zignSec.eid.baseurl:https://test.zignsec.com/v2/eid}", configuration = [ZignSecFeignConfig::class])
interface ZignSecClient {

    @PostMapping(value = ["/{bankid_selector}"], consumes = ["application/json"])
    fun auth(
        @PathVariable("bankid_selector") bankIdSelector: String,
        @RequestHeader("Authorization") authorization: String,
        @RequestHeader("Host") host: String,
        @RequestBody body: ZignSecRequestBody
    ): ZignSecResponse

    @GetMapping(value = ["/{session_id}"], consumes = ["application/json"])
    fun collect(
        @PathVariable("session_id") sessionId: UUID,
        @RequestHeader("Authorization") authorization: String,
        @RequestHeader("Host") host: String
    ): ZignSecCollectResponse

    @GetMapping(value = ["/{session_id}"], consumes = ["application/json"])
    fun scollect(
        @PathVariable("session_id") sessionId: UUID,
        @RequestHeader("Authorization") authorization: String,
        @RequestHeader("Host") host: String
    ): String
}

