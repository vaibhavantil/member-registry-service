package com.hedvig.external.bankID.bankIdRestTypes;

import lombok.Value;

@Value
public class OrderAuthResponse {
  protected String orderRef;
  protected String autoStartToken;
}
