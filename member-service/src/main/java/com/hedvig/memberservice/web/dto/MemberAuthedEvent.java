package com.hedvig.botService.web.dto;

import lombok.Value;

import java.time.Instant;
import java.util.UUID;

@Value
public class MemberAuthedEvent {

    private UUID eventId;
    private Long memberId;
    private Instant createdAt;


    public MemberAuthedEvent(Long memberId) {
        this.eventId = UUID.randomUUID();
        this.memberId = memberId;
        this.createdAt = Instant.now();
    }
}