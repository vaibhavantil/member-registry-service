package com.hedvig.memberservice.services.signing.simple.repository

import org.springframework.data.repository.CrudRepository
import java.util.UUID

interface SimpleSigningSessionRepository : CrudRepository<SimpleSignSession, UUID> {
    fun findByMemberId(memberId: Long): SimpleSignSession?
}
