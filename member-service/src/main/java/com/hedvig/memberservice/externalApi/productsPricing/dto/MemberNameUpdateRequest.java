package com.hedvig.memberservice.externalApi.productsPricing.dto;

import lombok.Value;

@Value
public class MemberNameUpdateRequest {
  String memberId;
  String name;
}
