package com.hedvig.memberservice.web

import com.hedvig.memberservice.services.QualityAssuranceServiceImpl
import com.hedvig.memberservice.web.dto.UnsignMemberMarket
import com.hedvig.memberservice.web.dto.UnsignMemberRequest
import org.springframework.context.annotation.Profile
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@Profile("staging", "development")
@RequestMapping("/_/staging/")
class QualityAssuranceController(
    private val qualityAssuranceService: QualityAssuranceServiceImpl
) {
    @PostMapping("{market}/unsignMember")
    fun unsignMember(
        @PathVariable("market") market: UnsignMemberMarket,
        @RequestBody request: UnsignMemberRequest
    ) = ResponseEntity.ok(
        qualityAssuranceService.unsignMember(
            market = market,
            ssn = request.ssn
        )
    )
}
