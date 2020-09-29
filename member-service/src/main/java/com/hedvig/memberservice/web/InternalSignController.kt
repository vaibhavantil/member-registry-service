package com.hedvig.memberservice.web

import com.hedvig.memberservice.services.UnderwriterSigningService
import com.hedvig.memberservice.web.dto.UnderwriterStartNorwegianBankIdSignSessionRequest
import com.hedvig.memberservice.web.dto.UnderwriterStartSwedishBankIdSignSessionRequest
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

    @PostMapping("swedish/bankid/{memberId}")
    fun startSwedishBankIdSign(
        @PathVariable("memberId") memberId: Long,
        @RequestBody request: UnderwriterStartSwedishBankIdSignSessionRequest
    ) = ResponseEntity.ok(
        underwriterSigningService.startSwedishBankIdSignSession(
            request.underwriterSessionReference,
            memberId,
            request.ssn,
            request.ipAddress,
            request.isSwitching
        )
    )

    @PostMapping("norwegian/bankid/{memberId}")
    fun startNorwegianSing(
        @PathVariable("memberId") memberId: Long,
        @RequestBody request: UnderwriterStartNorwegianBankIdSignSessionRequest
    ) = ResponseEntity.ok(
        underwriterSigningService.startNorwegianBankIdSignSession(
            request.underwriterSessionReference,
            memberId,
            request.ssn,
            request.successUrl,
            request.failUrl
        )
    )

    //TODO: danish test
}

