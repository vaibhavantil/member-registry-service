package com.hedvig.memberservice.services

import com.hedvig.memberservice.web.dto.UnsignMemberMarket

interface QualityAssuranceService {
    fun unsignMember(market: UnsignMemberMarket, ssn: String): Boolean
}
