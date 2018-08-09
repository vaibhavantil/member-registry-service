package com.hedvig.memberservice.query;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TrackingIdRepository extends JpaRepository<TrackingIdEntity, Long> {
  Optional<TrackingIdEntity> findByMemberId(Long s);

  Optional<TrackingIdEntity> findByTrackingId(UUID s);
}
