package com.hedvig.memberservice.enteties;

import java.util.Optional;
import org.springframework.data.repository.CrudRepository;

public interface SignSessionRepository extends CrudRepository<SignSession, String> {

  Optional<SignSession> findByOrderReference(final String orderReference);
  Optional<SignSession> findByMemberId(final Long memberId);

}