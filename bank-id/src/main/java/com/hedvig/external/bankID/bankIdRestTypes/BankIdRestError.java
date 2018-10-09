package com.hedvig.external.bankID.bankIdRestTypes;

import lombok.Value;

@Value
public class BankIdRestError extends RuntimeException {

  private BankIdRestErrorType type;

  public BankIdRestError(BankIdRestErrorType type) {
    super(type.name());
    this.type = type;
  }

  public BankIdRestError(BankIdRestErrorType type, String errorCode, String details) {
    super(errorCode + " " + details);
    this.type = type;
  }
}
