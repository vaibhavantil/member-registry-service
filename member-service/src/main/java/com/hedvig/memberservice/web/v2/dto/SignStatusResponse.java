package com.hedvig.memberservice.web.v2.dto;

import com.hedvig.memberservice.entities.SignSession;
import com.hedvig.memberservice.entities.SignStatus;
import javax.validation.constraints.NotNull;
import lombok.Value;
import lombok.val;

@Value
public class SignStatusResponse {


  @NotNull
  SignStatus status;

  CollectData collectData;


  public static SignStatusResponse CreateFromEntity(
      SignSession session) {

    val collectResponse = session.getCollectResponse();
    val data =
        collectResponse == null
            ? null
            : new CollectData(collectResponse.getStatus(), collectResponse.getHintCode());

    return new SignStatusResponse(session.getStatus(), data);
  }
}
