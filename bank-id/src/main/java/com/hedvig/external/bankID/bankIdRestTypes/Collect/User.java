package com.hedvig.external.bankID.bankIdRestTypes.Collect;

import lombok.Value;

@Value
public class User {
  private String personalNumber;
  private String name;
  private String givenName;
  private String surname;
}
