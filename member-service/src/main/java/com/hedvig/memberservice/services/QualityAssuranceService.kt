package com.hedvig.memberservice.services

interface QualityAssuranceService {
    fun unsignMember(ssn: String): Boolean
}
