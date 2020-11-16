package com.hedvig.memberservice.events;

import com.hedvig.memberservice.aggregates.MemberStatus;
import lombok.Value;

import java.time.Instant;

@Value
public class MemberCreatedEvent {

    public Long id;

    public MemberStatus status;

    public Instant createdOn;

    public MemberCreatedEvent(Long id, MemberStatus status) {
        this.id = id;
        this.status = status;
        this.createdOn = Instant.now();
    }
}
