package com.hedvig.memberservice.web.v2.dto;

import lombok.Value;
import lombok.val;

@Value
public class SignStatusResponse {

  CollectData collectData;

  public static SignStatusResponse CreateFromEntity(
      com.hedvig.memberservice.entities.CollectResponse collectResponse) {

    val data =
        collectResponse == null
            ? null
            : new CollectData(collectResponse.getStatus(), collectResponse.getHintCode());

    return new SignStatusResponse(data);
  }
}
