package com.hedvig.memberservice.events;

import lombok.Value;

import java.time.Instant;

@Value
public class MemberCancellationEvent {
    Long memberId;
    Instant inactivationDate;
}
