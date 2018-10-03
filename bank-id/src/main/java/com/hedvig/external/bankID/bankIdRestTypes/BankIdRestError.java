package com.hedvig.external.bankID.bankIdRestTypes;

import javax.validation.Valid;
import lombok.AllArgsConstructor;

@Valid
@AllArgsConstructor
public class BankIdRestError extends RuntimeException {

  private BankIdRestErrorType type;
  private String errorMessage;
  private String reason;

  public BankIdRestError(BankIdRestErrorType type) {
    this.type = type;
  }
}
