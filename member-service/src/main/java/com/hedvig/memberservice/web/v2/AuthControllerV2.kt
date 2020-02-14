package com.hedvig.memberservice.web.v2

import com.hedvig.memberservice.services.v2.BankIdServiceV2
import com.hedvig.memberservice.util.getEndUserIp
import com.hedvig.memberservice.web.v2.dto.AuthResponse
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/v2/member/bankid/")
class AuthControllerV2(
    private val bankIdService: BankIdServiceV2
) {
    private val log: Logger = LoggerFactory.getLogger(AuthControllerV2::class.java)

    @PostMapping("auth")
    fun auth(@RequestHeader("hedvig.token") memberId: Long, @RequestHeader(value = "x-forwarded-for", required = false) forwardedIp: String?): ResponseEntity<AuthResponse> {
        MDC.put("memberId", memberId.toString())

        val endUserIp = forwardedIp
            .getEndUserIp("Header 'x-forwarded-for' was not included when calling AuthControllerV2 auth! MemberId:$memberId")

        val status = bankIdService.auth(memberId, endUserIp)

        if(status.autoStartToken == null) {
            log.error("AutoStartToken is null for orderRef {}", status.orderRef)
        }

        return ResponseEntity.ok(AuthResponse(status.autoStartToken))
    }
}

