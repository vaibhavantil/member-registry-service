package com.hedvig.integration.contentservice

import com.hedvig.integration.contentservice.dto.CashbackOptionDTO
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable

@FeignClient(name = "contentService", url = "\${hedvig.contentService.url:content-service}")
interface ContentServiceClient {

    @GetMapping("_/cashback/{cashbackId}/{locale}/option")
    fun cashbackOption(
        @PathVariable("cashbackId") cashbackId: String,
        @PathVariable("locale") locale: String
    ) : ResponseEntity<CashbackOptionDTO>

    @GetMapping("_/cashback/{locale}/options")
    fun cashbackOptions(
        @PathVariable("locale") locale: String
    ) : ResponseEntity<List<CashbackOptionDTO>>
}

