package com.hedvig.memberservice.externalApi.productsPricing.dto;

import lombok.NonNull;
import lombok.Value;

@Value
public class EditMemberNameRequestDTO {
  @NonNull String memberId;
  @NonNull String firstName;
  @NonNull String lastName;
}
