package com.hedvig.external.bankID.bankIdRestTypes;

import lombok.Value;

@Value
public class BankIdRestErrorResponse {

  private String errorMessage;
  private String reason;
}
