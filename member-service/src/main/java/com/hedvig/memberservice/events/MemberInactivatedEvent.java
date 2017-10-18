package com.hedvig.memberservice.events;

import lombok.Value;

@Value
public class MemberInactivatedEvent {
    private final Long id;

    public MemberInactivatedEvent(Long id) {
        this.id = id;
    }
}
