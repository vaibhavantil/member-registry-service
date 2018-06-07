package com.hedvig.memberservice.query;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface MemberRepository extends PagingAndSortingRepository<MemberEntity, Long> {
  Optional<MemberEntity> findById(long s);

  Optional<MemberEntity> findBySsn(String s);

  @Query("select count(*) from MemberEntity m where m.status = 'SIGNED'")
  Long countSignedMembers();

  @Query(
      "SELECT m FROM MemberEntity m WHERE lower(m.firstName) LIKE lower(concat('%', :query, '%')) "
          + "OR lower(m.lastName) LIKE lower(concat('%', :query, '%'))")
  Page<MemberEntity> searchByQuery(@Param("query") String query, Pageable pageable);

  @Query("SELECT m FROM MemberEntity m WHERE status = :status")
  Page<MemberEntity> searchByStatus(@Param("status") String status, Pageable pageable);

  @Query(
      "SELECT m FROM MemberEntity m WHERE status = :status "
          + "AND (lower(m.firstName) LIKE lower(concat('%', :query, '%')) "
          + "OR lower(m.lastName) LIKE lower(concat('%', :query, '%')))")
  Page<MemberEntity> searchByStatusAndQuery(
      @Param("status") String status, @Param("query") String query, Pageable pageable);

  @Query("select m from MemberEntity m where m.id = :id")
  Page<MemberEntity> searchById(@Param("id") Long id, Pageable pageable);

  @Query("select m from MemberEntity m where m.id = :id and m.status = :status")
  Page<MemberEntity> searchByIdAndStatus(
      @Param("id") Long id, @Param("status") String status, Pageable pageable);
}
