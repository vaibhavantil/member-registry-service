package com.hedvig.memberservice.events;

import lombok.Value;

@Value
public class EmailUpdatedEvent {
    private final Long id;
    private final String email;

}
