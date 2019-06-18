package com.hedvig.memberservice.externalApi.productsPricing.dto;

import lombok.Value;

import java.time.Instant;

@Value
public class MemberCreatedRequest {
  String memberId;
}
