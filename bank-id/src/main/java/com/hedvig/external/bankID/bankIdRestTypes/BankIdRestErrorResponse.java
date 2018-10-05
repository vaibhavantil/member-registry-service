package com.hedvig.external.bankID.bankIdRestTypes;

import lombok.Value;

@Value
public class BankIdRestErrorResponse {

  private String errorCode;
  private String details;
}
