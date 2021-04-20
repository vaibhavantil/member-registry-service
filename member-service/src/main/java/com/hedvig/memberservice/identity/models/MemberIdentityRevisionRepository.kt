package com.hedvig.memberservice.identity.models

import org.springframework.data.jpa.repository.JpaRepository

interface MemberIdentityRevisionRepository : JpaRepository<MemberIdentityRevision, Long> {
    fun findByMemberId(memberId: Long): List<MemberIdentityRevision>
}
