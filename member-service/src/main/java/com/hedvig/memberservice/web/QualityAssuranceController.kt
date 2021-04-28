package com.hedvig.memberservice.web

import com.hedvig.memberservice.services.QualityAssuranceService
import com.hedvig.memberservice.web.dto.UnsignMemberRequest
import org.springframework.context.annotation.Profile
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@Profile("staging", "development")
@RequestMapping("/_/staging/")
class QualityAssuranceController(
    private val qualityAssuranceService: QualityAssuranceService
) {
    @PostMapping("unsignMember")
    fun unsignMember(
        @RequestBody request: UnsignMemberRequest
    ): ResponseEntity<Boolean> = ResponseEntity.ok(
        qualityAssuranceService.unsignMember(
            ssn = request.ssn
        )
    )
}
