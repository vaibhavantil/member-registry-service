package com.hedvig.memberservice.web

import com.hedvig.memberservice.services.SigningService
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController


@RestController
@RequestMapping("/_/contracts/")
class InternalContractsController(
    private val signingService: SigningService
) {

    @PostMapping("created/{memberId}")
    fun contractsCreated(
        @PathVariable("memberId") memberId: Long
    ) {
        signingService.notifyContractsCreated(memberId)
    }
}
