package com.hedvig.memberservice.events;

import com.hedvig.memberservice.aggregates.MemberStatus;
import java.time.Instant;
import lombok.Value;

@Value
public class MemberCreatedEvent {

  private Long id;

  private MemberStatus status;

  private Instant createdOn;

  public MemberCreatedEvent(Long id,  MemberStatus status){
    this.id = id;
    this.status = status;
    this.createdOn = Instant.now();
  }
}
