package com.hedvig.memberservice.events;

import java.time.Instant;
import lombok.Value;

@Value
public class MemberCancellationEvent {
  public Long memberId;
  public Instant inactivationDate;
}
