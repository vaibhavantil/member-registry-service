package com.hedvig.memberservice.events;

import java.util.UUID;
import lombok.Value;

@Value
public class TrackingIdCreatedEvent {
  private final Long memberId;
  private final UUID trackingId;

  public TrackingIdCreatedEvent(Long memberId, UUID trackingId) {
    this.memberId = memberId;
    this.trackingId = trackingId;
  }
}
