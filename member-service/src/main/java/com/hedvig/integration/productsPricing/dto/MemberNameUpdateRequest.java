package com.hedvig.integration.productsPricing.dto;

import lombok.Value;

@Value
public class MemberNameUpdateRequest {
  String memberId;
  String firstName;
}
