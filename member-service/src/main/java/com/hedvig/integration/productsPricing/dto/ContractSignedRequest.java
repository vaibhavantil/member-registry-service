package com.hedvig.integration.productsPricing.dto;

import java.time.Instant;
import lombok.Value;

@Value
public class ContractSignedRequest {
  String memberId;
  String referenceToken;
  String signature;
  String oscpResponse;
  Instant signedOn;
  String ssn;
}
