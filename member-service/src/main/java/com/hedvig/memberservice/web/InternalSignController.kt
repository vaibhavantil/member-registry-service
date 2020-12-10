package com.hedvig.memberservice.web

import com.hedvig.memberservice.services.signing.underwriter.UnderwriterSigningService
import com.hedvig.memberservice.web.dto.UnderwriterStartRedirectBankIdSignSessionRequest
import com.hedvig.memberservice.web.dto.UnderwriterStartSignSessionRequest
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

    @Deprecated("use `startSign`")
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

    @Deprecated("use `startSign`")
    @PostMapping("norwegian/bankid/{memberId}")
    fun startNorwegianSign(
        @PathVariable("memberId") memberId: Long,
        @RequestBody request: UnderwriterStartRedirectBankIdSignSessionRequest
    ) = ResponseEntity.ok(
        underwriterSigningService.startNorwegianBankIdSignSession(
            request.underwriterSessionReference,
            memberId,
            request.ssn,
            request.successUrl,
            request.failUrl
        )
    )

    @Deprecated("use `startSign`")
    @PostMapping("danish/bankid/{memberId}")
    fun startDanishSign(
        @PathVariable("memberId") memberId: Long,
        @RequestBody request: UnderwriterStartRedirectBankIdSignSessionRequest
    ) = ResponseEntity.ok(
        underwriterSigningService.startDanishBankIdSignSession(
            request.underwriterSessionReference,
            memberId,
            request.ssn,
            request.successUrl,
            request.failUrl
        )
    )

    @PostMapping("{memberId}")
    fun startSign(
        @PathVariable("memberId") memberId: Long,
        @RequestBody request: UnderwriterStartSignSessionRequest
    ) = ResponseEntity.ok(underwriterSigningService.startSign(memberId, request))
}

