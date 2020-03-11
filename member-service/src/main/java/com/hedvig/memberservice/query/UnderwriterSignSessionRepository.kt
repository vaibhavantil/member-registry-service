package com.hedvig.memberservice.query

import com.hedvig.memberservice.entities.UnderwriterSignSessionEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import java.util.*
import javax.transaction.Transactional

interface UnderwriterSignSessionRepository: JpaRepository<UnderwriterSignSessionEntity, UUID> {

    @Query("""
        FROM UnderwriterSignSessionEntity WHERE signReference = :signReference
    """)
    fun findBySignReference(signReference: UUID): UnderwriterSignSessionEntity?

    @Query("""
        FROM UnderwriterSignSessionEntity WHERE underwriterSignSessionReference = :underwriterSignSessionReference
    """)
    fun findByUnderwriterSignSessionReference(underwriterSignSessionReference: UUID): UnderwriterSignSessionEntity?

    @Modifying
    @Transactional
    @Query("""
        UPDATE UnderwriterSignSessionEntity session set session.underwriterSignSessionReference = ?1 where session.internalId = ?2
    """)
    fun updateUnderwriterSignSessionReference(underwriterSignSessionReference: UUID?, internalId: Long)
}

fun UnderwriterSignSessionRepository.saveOrUpdateReusableSession(underwriterSessionRef: UUID, signReference: UUID) {
    findBySignReference(signReference)?.let {
        updateUnderwriterSignSessionReference(underwriterSessionRef, it.internalId)
    } ?: save(UnderwriterSignSessionEntity(underwriterSessionRef, signReference))
}
