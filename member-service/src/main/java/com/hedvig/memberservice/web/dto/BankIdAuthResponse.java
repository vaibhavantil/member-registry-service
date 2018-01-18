package com.hedvig.memberservice.web.dto;


import lombok.Value;

@Value
public class BankIdAuthResponse {
    private String autoStartToken;
    private String referenceToken;
}
