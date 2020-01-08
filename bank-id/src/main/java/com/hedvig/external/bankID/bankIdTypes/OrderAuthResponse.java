package com.hedvig.external.bankID.bankIdTypes;

import lombok.Value;

@Value
public class OrderAuthResponse {
  protected String orderRef;
  protected String autoStartToken;
}
