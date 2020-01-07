package com.hedvig.external.bankID.bankIdTypes;

import lombok.Value;

@Value
public class BankIdErrorResponse {

  private String errorCode;
  private String details;
}
