package com.hedvig.memberservice.events;

import lombok.Value;

@Value
public class MemberAuthenticatedEvent {
    private Long memberId;
    private String bankIdReferenceToken;
}
