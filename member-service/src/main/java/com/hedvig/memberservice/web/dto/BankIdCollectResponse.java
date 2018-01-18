package com.hedvig.memberservice.web.dto;

import lombok.Value;

@Value
public class BankIdCollectResponse {
    private BankIdProgressStatus bankIdStatus;
    private String referenceToken;
    private String newMemberId;
}
