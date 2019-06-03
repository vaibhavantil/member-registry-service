package com.hedvig.memberservice.externalApi.productsPricing.dto;

import lombok.Value;

@Value
public class EditMemberNameDto {
  String memberId;
  String firstName;
  String lastName;
}
