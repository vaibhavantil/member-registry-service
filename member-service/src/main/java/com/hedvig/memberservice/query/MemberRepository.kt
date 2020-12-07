package com.hedvig.memberservice.query

import com.hedvig.memberservice.aggregates.MemberStatus
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface MemberRepository : JpaRepository<MemberEntity, Long> {

    fun findBySsn(ssn: String): List<MemberEntity>

    fun findAllByIdIn(ids: List<Long>): List<MemberEntity>

    @Query(
        """
      FROM MemberEntity
      WHERE status != 'SIGNED' AND (ssn = :ssn OR email = :email) AND id != :memberId
    """
    )
    fun findNonSignedBySsnOrEmailAndNotId(
        ssn: String,
        email: String,
        memberId: Long
    ): List<MemberEntity>

    @Query(
        """
        FROM MemberEntity
        WHERE (status = 'SIGNED' OR status='TERMINATED') AND (ssn = :ssn OR email = :email)
    """
    )
    fun findSignedMembersBySsnOrEmail(
        ssn: String?,
        email: String
    ): List<MemberEntity>

    @Query("select count(*) from MemberEntity m where m.status = 'SIGNED'")
    fun countSignedMembers(): Long?

    @Query(
        """
        SELECT m
        FROM MemberEntity m
        WHERE
            m.status IN ('SIGNED', 'TERMINATED')
        AND
            (
                CAST(m.id as text) = :query
                OR LOWER(TRIM(m.firstName)) LIKE ('%' || LOWER(:query) || '%')
                OR LOWER(TRIM(m.lastName)) LIKE ('%' || LOWER(:query) || '%')
                OR LOWER(TRIM(m.firstName) || ' ' || TRIM(m.lastName)) LIKE ('%' || LOWER(:query) || '%')
                OR m.ssn LIKE ('%' || :query || '%')
                OR LOWER(m.email) LIKE ('%' || LOWER(:query) || '%')
                OR m.phoneNumber LIKE ('%' || :query || '%')
            )
    """
    )
    fun searchSignedOrTerminated(
        @Param("query") query: String,
        p: Pageable
    ): Page<MemberEntity>

    @Query(
        """
        SELECT m
        FROM MemberEntity m
        WHERE
            CAST(m.id as text) = :query
            OR LOWER(TRIM(m.firstName)) LIKE ('%' || LOWER(:query) || '%')
            OR LOWER(TRIM(m.lastName)) LIKE ('%' || LOWER(:query) || '%')
            OR LOWER(TRIM(m.firstName) || ' ' || TRIM(m.lastName)) LIKE ('%' || LOWER(:query) || '%')
            OR m.ssn LIKE ('%' || :query || '%')
            OR LOWER(m.email) LIKE ('%' || LOWER(:query) || '%')
            OR m.phoneNumber LIKE ('%' || :query || '%')
    """
    )
    fun searchAll(
        @Param("query") query: String,
        p: Pageable
    ): Page<MemberEntity>

    fun findByStatus(status: MemberStatus): List<MemberEntity>

    fun findAllByStatusAndSsnNotIn(status: MemberStatus, ssns: List<String>): List<MemberEntity>

    @Query(
        """
        SELECT m.id from MemberEntity m WHERE m.pickedLocale is null and (first_name is null or first_name != 'GDPR')
    """
    )
    fun findIdsWithNoPickedLocale(pageable: Pageable): Page<Long>
}
