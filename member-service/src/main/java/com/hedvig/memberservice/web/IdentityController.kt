package com.hedvig.memberservice.web

import com.hedvig.memberservice.services.IdentityService
import com.hedvig.memberservice.web.dto.IdentityDto
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/_/member/identity/")
class IdentityController(
    private val identityService: IdentityService
) {

    @GetMapping("{memberId}")
    fun identity(
        @PathVariable("memberId") memberId: Long
    ): ResponseEntity<IdentityDto> =
        identityService.identityOnMemberId(memberId)?.let {
            ResponseEntity.ok(IdentityDto.from(it))
        } ?: ResponseEntity.notFound().build()
}

