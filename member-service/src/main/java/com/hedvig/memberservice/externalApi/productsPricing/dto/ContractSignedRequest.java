package com.hedvig.memberservice.externalApi.productsPricing.dto;

import lombok.Value;

@Value
public class ContractSignedRequest {
    String memberId;
    String referenceToken;
    String signature;
    String oscpResponse;
}
