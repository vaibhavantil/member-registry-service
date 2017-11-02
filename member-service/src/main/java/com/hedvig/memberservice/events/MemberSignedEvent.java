package com.hedvig.memberservice.events;

import lombok.Value;

@Value
public class MemberSignedEvent {

    private final Long id;
    private final String referenceId;

}