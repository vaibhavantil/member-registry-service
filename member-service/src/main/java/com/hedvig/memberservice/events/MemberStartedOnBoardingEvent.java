package com.hedvig.memberservice.events;

import com.hedvig.memberservice.aggregates.MemberStatus;
import lombok.Value;

@Value
public class MemberStartedOnBoardingEvent {
    private Long memberId;
    private MemberStatus newStatus;
}
