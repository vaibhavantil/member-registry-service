package com.hedvig.integration.productsPricing.dto;

import java.util.List;
import lombok.Value;

@Value
public class InsuranceStatusDTO {

  List<String> safetyIncreasers;
  String insuranceStatus;
}
