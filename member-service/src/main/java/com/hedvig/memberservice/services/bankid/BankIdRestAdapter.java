package com.hedvig.memberservice.services.bankid;

import static bankid.FaultStatusType.CANCELLED;
import static bankid.FaultStatusType.CERTIFICATE_ERR;
import static bankid.FaultStatusType.EXPIRED_TRANSACTION;
import static bankid.FaultStatusType.INTERNAL_ERROR;
import static bankid.FaultStatusType.START_FAILED;
import static bankid.FaultStatusType.USER_CANCEL;

import bankid.FaultStatusType;
import bankid.RpFaultType;
import com.hedvig.external.bankID.bankIdRest.BankIdRestApi;
import com.hedvig.external.bankID.bankIdRestTypes.CollectRequest;
import com.hedvig.external.bankID.bankIdRestTypes.CollectStatus;
import com.hedvig.external.bankID.bankIdRestTypes.CompletionData;
import com.hedvig.external.bankID.bankIdRestTypes.OrderAuthRequest;
import com.hedvig.external.bankID.bankIdRestTypes.OrderSignRequest;
import com.hedvig.external.bankID.bankidTypes.CollectResponse;
import com.hedvig.external.bankID.bankidTypes.OrderResponse;
import com.hedvig.external.bankID.bankidTypes.ProgressStatus;
import com.hedvig.external.bankID.bankidTypes.UserInfo;
import com.hedvig.external.bankID.exceptions.BankIDError;
import java.util.Base64;
import lombok.val;

public class BankIdRestAdapter implements BankIdApi {

  private final BankIdRestApi restApi;

  public BankIdRestAdapter(BankIdRestApi restApi) {
    this.restApi = restApi;
  }

  @Override
  public OrderResponse auth() {

    val response = restApi.auth(new OrderAuthRequest("95.192.15.216"));
    return new OrderResponse(response.getOrderRef(), response.getAutoStartToken());
  }

  @Override
  public OrderResponse sign(String ssn, String message) {

    val encodedMessage = Base64.getEncoder().encodeToString(message.getBytes());
    val response = restApi.sign(new OrderSignRequest(ssn, "95.192.15.216", encodedMessage));

    return new OrderResponse(response.getOrderRef(), response.getAutoStartToken());
  }

  @Override
  public CollectResponse authCollect(String orderRef) {

    return collect(orderRef);
  }

  @Override
  public CollectResponse signCollect(String orderRef) {

    return collect(orderRef);
  }

  private CollectResponse collect(String orderRef) {
    val response = restApi.collect(new CollectRequest(orderRef));

    if(response.getStatus() == CollectStatus.complete) {
      return new CollectResponse(
          createProgressStatus(response),
          response.getCompletionData().getSignature(),
          createUserInfo(response.getCompletionData()),
          response.getCompletionData().getOcspResponse());
    }


    return new CollectResponse(
        createProgressStatus(response),
        null,
        null,
        null
    );

  }

  private ProgressStatus createProgressStatus(
      com.hedvig.external.bankID.bankIdRestTypes.CollectResponse response) {

    if (response.getStatus() == CollectStatus.complete) {
      return ProgressStatus.COMPLETE;
    }

    switch (response.getHintCode()) {
      case "outstandingTransaction":
        {
          return ProgressStatus.OUTSTANDING_TRANSACTION;
        }
      case "noClient":
        {
          return ProgressStatus.NO_CLIENT;
        }
      case "started":
        {
          return ProgressStatus.STARTED;
        }
      case "userSign":
        {
          return ProgressStatus.USER_SIGN;
        }
      case "expiredTransaction":
        {
          throw createBankIdError(
              EXPIRED_TRANSACTION,
              "The order has expired. The BankID security app/program did not start, the user did not finalize the "
                  + "signing or the RP called collect too late");
        }
      case "certificateErr":
        {
          throw createBankIdError(
              CERTIFICATE_ERR,
              "This error is returned if: 1) The user has entered wrong security code too many "
                  + "times. The BankID cannot be used. 2) The users BankID is revoked. 3) The users BankID is invalid");
        }
      case "userCancel":
        {
          throw createBankIdError(USER_CANCEL, "userCancel");
        }
      case "cancelled":
        {
          throw createBankIdError(
              CANCELLED, "The order was cancelled. The system received a new order for the user");
        }
      case "startFailed":
        {
          throw createBankIdError(START_FAILED, "Session did not start");
        }

      default:
        {
          throw createBankIdError(
              INTERNAL_ERROR, String.format("Unkown hintCode: %s", response.getHintCode()));
        }
    }
  }

  private BankIDError createBankIdError(FaultStatusType faultStatusType, String message) {
    val fault = new RpFaultType();
    fault.setFaultStatus(faultStatusType);
    fault.setDetailedDescription(message);
    return new BankIDError(fault);
  }

  private UserInfo createUserInfo(final CompletionData data) {
    return new UserInfo(
        data.getUser().getName(),
        data.getUser().getGivenName(),
        data.getUser().getSurname(),
        data.getUser().getPersonalNumber(),
        data.getDevice().getIpAddress(),
        Long.valueOf(data.getCert().getNotBefore()),
        Long.valueOf(data.getCert().getNotAfter()));
  }
}
