package com.hedvig.memberservice.web.dto;

import lombok.Value;

@Value
public class UpdatePhoneNumberRequest {
  private String phoneNumber;
}
