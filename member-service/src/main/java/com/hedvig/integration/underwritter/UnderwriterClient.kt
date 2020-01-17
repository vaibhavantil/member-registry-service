package com.hedvig.integration.underwritter

import com.hedvig.integration.underwritter.dtos.SignRequest
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody

@FeignClient(name = "underwriter", url = "\${hedvig.underwriter.url:underwriter}")
interface UnderwriterClient {
    @PostMapping("/member/{memberId}/signed")
    fun memberSigned(@PathVariable memberId: String, @RequestBody signRequest: SignRequest): ResponseEntity<Void>
}
