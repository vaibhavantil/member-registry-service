package com.hedvig.memberservice.web.v2.dto;

import javax.validation.constraints.NotNull;
import lombok.Value;

@Value
public class SignStatusRequest {

  @NotNull
  String orderRef;

}
