package com.hedvig.external.bankID.bankIdRestTypes;

import lombok.AllArgsConstructor;
import lombok.Value;

@Value
@AllArgsConstructor
public class BankIdRestError extends RuntimeException {

  private BankIdRestErrorType type;
  private String errorMessage;
  private String reason;

  public BankIdRestError(BankIdRestErrorType type) {
    this.type = type;
  }
}
