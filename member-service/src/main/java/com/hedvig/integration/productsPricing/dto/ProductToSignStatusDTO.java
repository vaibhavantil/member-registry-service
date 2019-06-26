package com.hedvig.integration.productsPricing.dto;

import lombok.Value;

@Value
public class ProductToSignStatusDTO {

  boolean eligibleToSign;
  boolean switching;
}
