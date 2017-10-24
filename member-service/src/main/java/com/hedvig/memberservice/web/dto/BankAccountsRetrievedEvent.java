package com.hedvig.memberservice.web.dto;

import lombok.Value;

import java.time.Instant;

@Value
public class BankAccountsRetrievedEvent {

    private Long memberId;
    private Instant createdAt;
    private BankAccountDetailsList payload;

}
