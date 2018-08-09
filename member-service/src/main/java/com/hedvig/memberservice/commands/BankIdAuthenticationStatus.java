package com.hedvig.memberservice.commands;

import lombok.Data;

@Data
public class BankIdAuthenticationStatus {

  String SSN;

  String referenceToken;

  String surname;
  String givenName;
}
