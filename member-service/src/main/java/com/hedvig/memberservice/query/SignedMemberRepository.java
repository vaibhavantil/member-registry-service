package com.hedvig.memberservice.query;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SignedMemberRepository extends JpaRepository<SignedMemberEntity, Long> {
    Optional<SignedMemberEntity> findById(long s);
    Optional<SignedMemberEntity> findBySsn(String s);
}
