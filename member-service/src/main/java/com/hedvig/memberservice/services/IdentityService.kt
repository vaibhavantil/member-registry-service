package com.hedvig.memberservice.services

import com.hedvig.memberservice.identity.repository.IdentityEntity

interface IdentityService {
    fun identityOnMemberId(memberId: Long): IdentityEntity?
}
