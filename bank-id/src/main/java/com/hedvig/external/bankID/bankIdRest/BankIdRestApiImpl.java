package com.hedvig.external.bankID.bankIdRest;

import com.hedvig.external.bankID.bankIdRestTypes.BankIdRestError;
import com.hedvig.external.bankID.bankIdRestTypes.BankIdRestErrorType;
import com.hedvig.external.bankID.bankIdRestTypes.CollectRequest;
import com.hedvig.external.bankID.bankIdRestTypes.CollectResponse;
import com.hedvig.external.bankID.bankIdRestTypes.OrderAuthRequest;
import com.hedvig.external.bankID.bankIdRestTypes.OrderResponse;
import com.hedvig.external.bankID.bankIdRestTypes.OrderSignRequest;
import feign.FeignException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
public class BankIdRestApiImpl implements BankIdRestApi {

  private final BankIdRestClient bankIdRestClient;
  private Logger logger = LoggerFactory.getLogger(BankIdRestApiImpl.class);

  public BankIdRestApiImpl(BankIdRestClient bankIdRestClient) {
    this.bankIdRestClient = bankIdRestClient;
  }

  @Override
  public OrderResponse auth(OrderAuthRequest request) {
    try {
      ResponseEntity<?> response = bankIdRestClient.auth(request);
      return (OrderResponse) response.getBody();
    } catch (FeignException ex) {
      logger.error(
          "Auth - Something went wrong that wasn't mapped with the BankIdRestErrorDecoder. Status: {} , Message: {}",
          ex.status(), ex.getMessage());
      throw new BankIdRestError(BankIdRestErrorType.UNKNOWN, String.valueOf(ex.status()),
          ex.getMessage());
    }
  }

  @Override
  public OrderResponse sign(String personalNumber, String endUserIp, String userVisibleData) {
    try {
      ResponseEntity<?> response = bankIdRestClient.sign(new OrderSignRequest(personalNumber, endUserIp, userVisibleData));
      return (OrderResponse) response.getBody();
    } catch (FeignException ex) {
      logger.error(
          "Sign - Something went wrong that wasn't mapped with the BankIdRestErrorDecoder. Status: {} , Message: {}",
          ex.status(), ex.getMessage());
      throw new BankIdRestError(BankIdRestErrorType.UNKNOWN, String.valueOf(ex.status()),
          ex.getMessage());
    }
  }

  @Override
  public CollectResponse collect(CollectRequest request) {
    try {
      ResponseEntity<?> response = bankIdRestClient.collect(request);
      return (CollectResponse) response.getBody();
    } catch (FeignException ex) {
      logger.error(
          "Collect - Something went wrong that wasn't mapped with the BankIdRestErrorDecoder. Status: {} , Message: {}",
          ex.status(), ex.getMessage());
      throw new BankIdRestError(BankIdRestErrorType.UNKNOWN, String.valueOf(ex.status()),
          ex.getMessage());
    }
  }
}
