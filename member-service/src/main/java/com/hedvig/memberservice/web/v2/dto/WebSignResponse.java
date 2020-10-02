package com.hedvig.memberservice.web.v2.dto;

import com.hedvig.external.bankID.bankIdTypes.OrderResponse;
import com.hedvig.memberservice.entities.SignStatus;
import lombok.NonNull;
import lombok.Value;

@Value
public class WebSignResponse {

  @NonNull
  Long signId;
  SignStatus status;
  OrderResponse bankIdOrderResponse;
}
