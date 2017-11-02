package com.hedvig.memberservice.events;

import lombok.Value;

@Value
public class SSNUpdatedEvent {
    private final Long memberId;
    private final String ssn;

    public SSNUpdatedEvent(Long memberId, String ssn) {
        this.memberId = memberId;
        this.ssn = ssn;
    }
}
