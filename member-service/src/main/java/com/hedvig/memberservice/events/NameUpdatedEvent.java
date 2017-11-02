package com.hedvig.memberservice.events;

import lombok.Value;

@Value
public class NameUpdatedEvent {
    private final Long memberId;
    private final String firstName;
    private final String lastName;

    public NameUpdatedEvent(Long memberId, String firstName, String lastName) {

        this.memberId = memberId;
        this.firstName = firstName;
        this.lastName = lastName;
    }
}
