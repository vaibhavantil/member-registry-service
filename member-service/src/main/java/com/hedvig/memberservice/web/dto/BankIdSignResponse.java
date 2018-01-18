package com.hedvig.memberservice.web.dto;

import lombok.Value;

@Value
public class BankIdSignResponse {
    private final String autoStartToken;
    private final String referenceToken;
}
