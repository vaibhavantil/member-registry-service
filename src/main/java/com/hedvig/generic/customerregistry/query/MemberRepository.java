package com.hedvig.generic.customerregistry.query;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<MemberEntity, String> {
    Optional<MemberEntity> findById(String s);
    Optional<MemberEntity> findBypersonalIdentificationNumber(String s);
}
