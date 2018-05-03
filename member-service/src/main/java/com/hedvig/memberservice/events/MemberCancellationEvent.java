package com.hedvig.memberservice.events;

import lombok.Value;

import java.time.ZonedDateTime;

@Value
public class MemberCancellationEvent {
    Long memberId;
    ZonedDateTime inactivationDate;
}
