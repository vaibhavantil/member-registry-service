package com.hedvig.memberservice.web.dto;

import com.hedvig.external.billectaAPI.api.BankIdStatusType;
import lombok.Value;

@Value
public class BankIdSignResponse {

    private final String autoStartToken;
    private final String referenceToken;
    private final String status;
}
