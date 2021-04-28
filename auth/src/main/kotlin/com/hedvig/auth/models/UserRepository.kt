package com.hedvig.auth.models

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
internal interface UserRepository : JpaRepository<User, UUID> {
    fun findByAssociatedMemberId(memberId: String): User?
}
