package com.hedvig.memberservice.query

import com.hedvig.integration.underwriter.dtos.SignMethod
import com.hedvig.memberservice.entities.UnderwriterSignSessionEntity
import com.hedvig.memberservice.services.signing.underwriter.strategy.SignStrategy
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

    @Query
    fun findTopByMemberIdOrderByInternalIdDesc(memberId: Long): UnderwriterSignSessionEntity?
}

fun UnderwriterSignSessionRepository.saveOrUpdateReusableSession(underwriterSessionRef: UUID, signReference: UUID, memberId: Long, signStrategy: SignStrategy) {
    findBySignReference(signReference)?.let {
        updateUnderwriterSignSessionReference(underwriterSessionRef, it.internalId)
    } ?: save(UnderwriterSignSessionEntity(underwriterSessionRef, signReference, memberId, signStrategy))
}
