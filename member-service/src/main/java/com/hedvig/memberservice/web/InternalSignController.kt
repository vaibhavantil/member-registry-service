package com.hedvig.memberservice.web

import com.hedvig.memberservice.services.signing.underwriter.UnderwriterSigningService
import com.hedvig.memberservice.web.dto.UnderwriterStartSignSessionRequest
import com.hedvig.memberservice.web.dto.UnderwriterStartSignSessionResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/_/member/start/sign/")
class InternalSignController(
    private val underwriterSigningService: UnderwriterSigningService
) {
    @PostMapping("{memberId}")
    fun startSign(
        @PathVariable("memberId") memberId: Long,
        @RequestBody request: UnderwriterStartSignSessionRequest
    ): ResponseEntity<UnderwriterStartSignSessionResponse> =
        ResponseEntity.ok(underwriterSigningService.startSign(memberId, request))
}
