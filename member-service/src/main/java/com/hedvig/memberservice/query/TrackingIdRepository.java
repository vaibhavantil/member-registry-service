package com.hedvig.memberservice.query;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface TrackingIdRepository extends JpaRepository<TrackingIdEntity, Long> {
    Optional<TrackingIdEntity> findById(long s);
    Optional<TrackingIdEntity> findBySsn(String s);
    Optional<TrackingIdEntity> findByTrackingId(UUID s);
}
