package com.hedvig.memberservice.web.v2.dto;

import com.hedvig.external.authentication.dto.NorwegianBankIdProgressStatus;
import com.hedvig.memberservice.entities.SignSession;
import com.hedvig.memberservice.entities.SignStatus;
import javax.validation.constraints.NotNull;
import lombok.Value;
import lombok.val;
import org.jetbrains.annotations.Nullable;

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

    SignStatus status = session.getStatus();
    if (status == SignStatus.COMPLETED) {
      status = session.getIsContractsCreated() ? SignStatus.COMPLETED : SignStatus.IN_PROGRESS;
    }

    return new SignStatusResponse(status, data);
  }

  public static SignStatusResponse CreateFromNorwegianStatus(@NotNull NorwegianBankIdProgressStatus status) {
    switch (status) {
      case INITIATED:
        return new SignStatusResponse(SignStatus.INITIATED, null);
      case IN_PROGRESS:
        return new SignStatusResponse(SignStatus.IN_PROGRESS, null);
      case FAILED:
        return new SignStatusResponse(SignStatus.FAILED, null);
      case COMPLETED:
        return new SignStatusResponse(SignStatus.COMPLETED, null);
    }

    throw new RuntimeException("Could not return SignStatusResponse from NorwegianBankIdProgressStatus: " + status +".");
  }
}
