package com.hedvig.memberservice.web.dto;

import com.hedvig.external.bankID.bankidTypes.ProgressStatus;
import lombok.Value;

@Value
public class BankIdCollectResponse {
    private ProgressStatus bankIdStatus;
    private String autoStartToken;
    private String referenceToken;
    private String newMemberId;
}
