package com.hedvig.integration.underwriter

import com.hedvig.integration.underwriter.dtos.QuoteDto
import com.hedvig.integration.underwriter.dtos.SignRequest
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import java.util.*

@FeignClient(name = "underwriter", url = "\${hedvig.underwriter.url:underwriter}")
interface UnderwriterClient {
    @PostMapping("/_/v1/quotes/member/{memberId}/signed")
    fun memberSigned(@PathVariable memberId: String, @RequestBody signRequest: SignRequest): ResponseEntity<Void>

    @GetMapping("/_/v1/quotes/members/{memberId}/latestQuote")
    fun getQuoteFromMemberId(@PathVariable memberId: String): ResponseEntity<QuoteDto>

    @PostMapping("/_/v1/signSession/swedishBankid/{sessionId}/completed")
    fun swedishBankIdSingComplete(@PathVariable sessionId: UUID, @RequestBody signRequest: SignRequest): ResponseEntity<Void>

    @PostMapping("/_/v1/signSession/{sessionId}/completed")
    fun singSessionComplete(@PathVariable sessionId: UUID): ResponseEntity<Void>
}
