package com.hedvig.generic.customerregistry.query;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<MemberEntity, Long> {
    Optional<MemberEntity> findById(long s);
    Optional<MemberEntity> findBySsn(String s);
}
