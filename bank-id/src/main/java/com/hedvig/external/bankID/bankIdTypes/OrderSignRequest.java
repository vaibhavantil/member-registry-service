package com.hedvig.external.bankID.bankIdTypes;

import lombok.Value;

@Value
public class OrderSignRequest {
  private String personalNumber;
  private String endUserIp;
  private String userVisibleData;
}
