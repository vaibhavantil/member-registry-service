package com.hedvig.memberservice.externalApi.productsPricing.dto;

import lombok.Value;

@Value
public class ProductToSignStatusDTO {

  boolean eligibleToSign;
  boolean switching;
}
