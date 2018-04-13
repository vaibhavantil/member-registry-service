package com.hedvig.memberservice.query;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.stream.Stream;

public interface MemberRepository extends JpaRepository<MemberEntity, Long> {
    Optional<MemberEntity> findById(long s);

    Optional<MemberEntity> findBySsn(String s);

    @Query("SELECT m FROM MemberEntity m")
    Stream<MemberEntity> searchAll();

    @Query("SELECT m FROM MemberEntity m WHERE lower(m.firstName) LIKE lower(concat('%', :query, '%')) " +
            "OR lower(m.lastName) LIKE lower(concat('%', :query, '%'))")
    Stream<MemberEntity> searchByQuery(@Param("query") String query);

    @Query("SELECT m FROM MemberEntity m WHERE status = :status")
    Stream<MemberEntity> searchByStatus(@Param("status") String status);

    @Query("SELECT m FROM MemberEntity m WHERE status = :status " +
            "AND (lower(m.firstName) LIKE lower(concat('%', :query, '%')) " +
            "OR lower(m.lastName) LIKE lower(concat('%', :query, '%')))")
    Stream<MemberEntity> searchByStatusAndQuery(@Param("status") String status, @Param("query") String query);

    @Query("select m from MemberEntity m where m.id = :id")
    Stream<MemberEntity> searchById(@Param("id") Long id);

    @Query("select m from MemberEntity m where m.id = :id and m.status = :status")
    Stream<MemberEntity> searchByIdAndStatus(@Param("id") Long id, @Param("status") String status);
}
