package com.hedvig.memberservice.services

import com.hedvig.memberservice.identity.repository.IdentityRepository
import com.hedvig.memberservice.services.IdentityService
import org.springframework.stereotype.Service

@Service
class IdentityServiceImpl(
    private val repository: IdentityRepository
): IdentityService {
    override fun identityOnMemberId(memberId: Long) = repository.findById(memberId).get()
}
