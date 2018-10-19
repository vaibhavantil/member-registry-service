package com.hedvig.memberservice.query;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import com.hedvig.memberservice.aggregates.MemberStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MemberRepository extends JpaRepository<MemberEntity, Long> {

  Optional<MemberEntity> findBySsn(String s);

  List<MemberEntity> findAllByIdIn(List<Long> ids);

  @Query("select count(*) from MemberEntity m where m.status = 'SIGNED'")
  Long countSignedMembers();

  @Query("SELECT m FROM MemberEntity m")
  Page<MemberEntity> searchAll(Pageable p);

  @Query(
      "SELECT m FROM MemberEntity m WHERE lower(m.firstName) LIKE lower(concat('%', :query, '%')) "
          + "OR lower(m.lastName) LIKE lower(concat('%', :query, '%'))")
  Page<MemberEntity> searchByQuery(@Param("query") String query, Pageable p);

  @Query("SELECT m FROM MemberEntity m WHERE status = :status")
  Page<MemberEntity> searchByStatus(@Param("status") MemberStatus status, Pageable p);

  @Query(
      "SELECT m FROM MemberEntity m WHERE status = :status "
          + "AND (lower(m.firstName) LIKE lower(concat('%', :query, '%')) "
          + "OR lower(m.lastName) LIKE lower(concat('%', :query, '%')))")
  Page<MemberEntity> searchByStatusAndQuery(
      @Param("status") MemberStatus status, @Param("query") String query, Pageable p);

  @Query("select m from MemberEntity m where m.id = :id")
  Page<MemberEntity> searchById(@Param("id") Long id, Pageable p);

  @Query("select m from MemberEntity m where m.id = :id and m.status = :status")
  Page<MemberEntity> searchByIdAndStatus(@Param("id") Long id, @Param("status") MemberStatus status, Pageable p);
}
