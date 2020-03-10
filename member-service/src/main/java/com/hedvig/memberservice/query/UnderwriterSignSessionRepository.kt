package com.hedvig.memberservice.query

import com.hedvig.memberservice.entities.UnderwriterSignSessionEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.util.*

interface UnderwriterSignSessionRepository: JpaRepository<UnderwriterSignSessionEntity, UUID> {

    @Query("""
        FROM UnderwriterSignSessionEntity WHERE signReference = :signReference
    """)
    fun findBySignReference(signReference: UUID): UnderwriterSignSessionEntity?
}

