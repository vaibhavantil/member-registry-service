package com.hedvig.memberservice.services

interface QualityAssuranceService {
    fun unsignMember(country: String, ssn: String): Boolean
}
