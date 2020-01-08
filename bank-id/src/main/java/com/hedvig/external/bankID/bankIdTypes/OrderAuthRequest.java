package com.hedvig.external.bankID.bankIdTypes;

import lombok.Value;

@Value
public class OrderAuthRequest {
  private String endUserIp;
}
