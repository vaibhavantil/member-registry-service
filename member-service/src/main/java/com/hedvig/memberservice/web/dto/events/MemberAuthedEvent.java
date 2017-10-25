package com.hedvig.memberservice.web.dto.events;

import com.hedvig.memberservice.web.dto.Member;
import lombok.Value;

import java.time.Instant;

@Value
public class MemberAuthedEvent {

    private String eventId;
    private Long memberId;
    private Instant createdAt;
    private Member member;


    public MemberAuthedEvent(Long memberId, String eventId, Instant timestamp, Member member) {
        this.eventId = eventId;
        this.memberId = memberId;
        this.createdAt = timestamp;
        this.member = member;
    }
}