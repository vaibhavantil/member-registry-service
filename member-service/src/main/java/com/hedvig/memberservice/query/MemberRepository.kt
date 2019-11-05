package com.hedvig.memberservice.query

import com.hedvig.memberservice.aggregates.MemberStatus
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.*

interface MemberRepository : JpaRepository<MemberEntity, Long> {

    fun findBySsn(ssn: String): Optional<MemberEntity>

    fun findAllByIdIn(ids: List<Long>): List<MemberEntity>

    @Query("""
      FROM MemberEntity
      WHERE status != 'SIGNED' AND (ssn = :ssn OR email = :email) AND id != :memberId
    """)
    fun findNonSignedBySsnOrEmailAndNotId(
        ssn: String,
        email: String,
        memberId: Long
    ): List<MemberEntity>

    @Query("select count(*) from MemberEntity m where m.status = 'SIGNED'")
    fun countSignedMembers(): Long?

    @Query("SELECT m FROM MemberEntity m")
    fun searchAll(p: Pageable): Page<MemberEntity>

    @Query("SELECT m FROM MemberEntity m WHERE lower(m.firstName) LIKE lower(concat('%', :query, '%')) " + "OR lower(m.lastName) LIKE lower(concat('%', :query, '%'))")
    fun searchByQuery(
        @Param("query") query: String,
        p: Pageable
    ): Page<MemberEntity>

    @Query("SELECT m FROM MemberEntity m WHERE status = :status")
    fun searchByStatus(
        @Param("status") status: MemberStatus,
        p: Pageable
    ): Page<MemberEntity>

    @Query("SELECT m FROM MemberEntity m WHERE status = :status "
        + "AND (lower(m.firstName) LIKE lower(concat('%', :query, '%')) "
        + "OR lower(m.lastName) LIKE lower(concat('%', :query, '%')))")
    fun searchByStatusAndQuery(
        @Param("status") status: MemberStatus,
        @Param("query") query: String,
        p: Pageable
    ): Page<MemberEntity>

    @Query("select m from MemberEntity m where m.id = :id")
    fun searchById(
        @Param("id") id: Long?,
        p: Pageable
    ): Page<MemberEntity>

    @Query("select m from MemberEntity m where m.id = :id and m.status = :status")
    fun searchByIdAndStatus(
        @Param("id") id: Long?,
        @Param("status") status: MemberStatus,
        p: Pageable
    ): Page<MemberEntity>

    fun findByStatus(status: MemberStatus): List<MemberEntity>
}
