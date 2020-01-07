package com.hedvig.memberservice.web.v2.dto;

import com.hedvig.external.bankID.bankIdTypes.CollectStatus;
import javax.validation.constraints.NotNull;
import lombok.Value;

@Value
public class CollectData {

  @NotNull
  CollectStatus status;

  String hintCode;
}
