package com.hedvig.memberservice.query;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SignedMemberRepository extends JpaRepository<SignedMemberEntity, Long> {
  Optional<SignedMemberEntity> findById(long s);

  Optional<SignedMemberEntity> findBySsn(String s);
}
