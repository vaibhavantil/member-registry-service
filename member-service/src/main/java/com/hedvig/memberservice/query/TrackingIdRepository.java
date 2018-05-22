package com.hedvig.memberservice.query;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TrackingIdRepository extends JpaRepository<TrackingIdEntity, Long> {
    Optional<TrackingIdEntity> findById(long s);
    Optional<TrackingIdEntity> findByTrackingId(UUID s);
}
