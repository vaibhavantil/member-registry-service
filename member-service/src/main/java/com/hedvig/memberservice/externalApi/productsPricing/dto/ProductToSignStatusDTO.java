package com.hedvig.memberservice.externalApi.productsPricing.dto;

import lombok.Value;

@Value
public class ProductToSignStatusDTO {

  private boolean elgibleToSign;
  private boolean switching;
}
