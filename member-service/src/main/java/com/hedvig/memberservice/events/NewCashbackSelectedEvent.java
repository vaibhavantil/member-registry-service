package com.hedvig.memberservice.events;

import lombok.Value;

@Value
public class NewCashbackSelectedEvent {
    private final Long memberId;
    private final String cashbackId;

    public NewCashbackSelectedEvent(Long memberId, String cashbackId) {
        this.memberId = memberId;
        this.cashbackId = cashbackId;
    }
}
