package com.hedvig.memberservice.enteties;

import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SignSessionRepository extends CrudRepository<SignSession, String> {

  @Query("select s from SignSession s where s.bankIdSession.orderReference = :orderReference")
  Optional<SignSession> findByOrderReference(final String orderReference);
  Optional<SignSession> findByMemberId(final Long memberId);

}