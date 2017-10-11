package com.hedvig.memberservice.events;

import com.hedvig.memberservice.aggregates.MemberStatus;
import lombok.Value;

@Value
public class MemberCreatedEvent {

    private Long id;

    private MemberStatus status;

}
