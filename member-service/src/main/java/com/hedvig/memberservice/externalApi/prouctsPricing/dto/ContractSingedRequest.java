package com.hedvig.memberservice.externalApi.prouctsPricing.dto;

import lombok.Value;

@Value
public class ContractSingedRequest {
    String memberId;
    String referenceToken;
}
