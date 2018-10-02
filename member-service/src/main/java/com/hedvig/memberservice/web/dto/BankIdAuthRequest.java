package com.hedvig.memberservice.web.dto;

import lombok.Value;

@Value
public class BankIdAuthRequest {

  private String ipAddress;
  private String memberId;
}
