package com.hedvig.external.bankID.bankIdTypes;

import lombok.Value;

@Value
public class BankIdError extends RuntimeException {

  private BankIdErrorType type;

  public BankIdError(BankIdErrorType type) {
    super(type.name());
    this.type = type;
  }

  public BankIdError(BankIdErrorType type, String errorCode, String details) {
    super(errorCode + " " + details);
    this.type = type;
  }
}
