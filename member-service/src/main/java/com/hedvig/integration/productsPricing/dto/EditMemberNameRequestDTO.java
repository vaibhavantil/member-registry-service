package com.hedvig.integration.productsPricing.dto;

import lombok.NonNull;
import lombok.Value;

@Value
public class EditMemberNameRequestDTO {
  @NonNull String memberId;
  @NonNull String firstName;
  @NonNull String lastName;
}
