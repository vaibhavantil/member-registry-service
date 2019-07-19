package com.hedvig.memberservice.web.v2

import com.hedvig.memberservice.services.v2.BankIdService
import com.hedvig.memberservice.web.v2.dto.AuthResponse
import org.axonframework.commandhandling.gateway.CommandGateway
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/v2/member/bankid/")
class AuthController(
        private val commandGateway: CommandGateway,
        private val bankIdService: BankIdService
) {
    @PostMapping("auth")
    fun auth(@RequestHeader("hedvig.token") memberId: Long): ResponseEntity<AuthResponse> {
        val status = bankIdService.auth(memberId)
        return ResponseEntity.ok(AuthResponse(status.autoStartToken))
    }
}

