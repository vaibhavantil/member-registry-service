package com.hedvig.memberservice.externalApi.prouctsPricing.dto;

import lombok.Value;

@Value
public class ContractSignedRequest {
    String memberId;
    String referenceToken;
}
