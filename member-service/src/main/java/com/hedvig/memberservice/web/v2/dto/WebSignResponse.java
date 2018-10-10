package com.hedvig.memberservice.web.v2.dto;

import com.hedvig.external.bankID.bankIdRestTypes.OrderResponse;
import com.hedvig.memberservice.enteties.SignStatus;
import lombok.NonNull;
import lombok.Value;

@Value
public class WebSignResponse {

  @NonNull
  Long signId;
  SignStatus status;
  OrderResponse bankIdOrderResponse;

}
