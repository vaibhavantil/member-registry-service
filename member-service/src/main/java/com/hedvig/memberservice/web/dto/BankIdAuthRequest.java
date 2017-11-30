package com.hedvig.memberservice.web.dto;

import lombok.Value;

@Value
public class BankIdAuthRequest {

    private String ssn;
    private String memberId;

}
