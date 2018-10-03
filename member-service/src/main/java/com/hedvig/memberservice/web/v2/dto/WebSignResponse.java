package com.hedvig.memberservice.web.v2.dto;

import com.hedvig.external.bankID.bankIdRestTypes.OrderResponse;
import lombok.Value;

@Value
public class WebSignResponse {

  OrderResponse bankIdOrderResponse;

}
