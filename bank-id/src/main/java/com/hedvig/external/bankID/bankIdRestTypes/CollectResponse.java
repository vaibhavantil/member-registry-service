package com.hedvig.external.bankID.bankIdRestTypes;

import lombok.Value;

@Value
public class CollectResponse {
  private String orderRef;
  private CollectStatus status;
  private String hintCode;
  private CompletionData completionData;
}
