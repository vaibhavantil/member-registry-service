package com.hedvig.memberservice.services.member.dto;

import com.hedvig.external.bankID.bankIdRestTypes.OrderResponse;
import javax.validation.constraints.NotNull;
import lombok.Value;

@Value
public class MemberSignResponse {

  @NotNull
  OrderResponse bankIdOrderResponse;

}
