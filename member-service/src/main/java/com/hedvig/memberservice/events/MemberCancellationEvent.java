package com.hedvig.memberservice.events;

import java.time.Instant;
import lombok.Value;

@Value
public class MemberCancellationEvent {
  Long memberId;
  Instant inactivationDate;
}
