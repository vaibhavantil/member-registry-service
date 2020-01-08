package com.hedvig.external.bankID.bankId;

import com.hedvig.external.bankID.bankIdTypes.BankIdError;
import com.hedvig.external.bankID.bankIdTypes.BankIdErrorType;
import com.hedvig.external.bankID.bankIdTypes.CollectRequest;
import com.hedvig.external.bankID.bankIdTypes.CollectResponse;
import com.hedvig.external.bankID.bankIdTypes.OrderAuthRequest;
import com.hedvig.external.bankID.bankIdTypes.OrderResponse;
import com.hedvig.external.bankID.bankIdTypes.OrderSignRequest;
import feign.FeignException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
public class BankIdApiImpl implements BankIdApi {

  private final BankIdClient bankIdClient;
  private Logger logger = LoggerFactory.getLogger(BankIdApiImpl.class);

  public BankIdApiImpl(BankIdClient bankIdClient) {
    this.bankIdClient = bankIdClient;
  }

  @Override
  public OrderResponse auth(OrderAuthRequest request) {
    try {
      ResponseEntity<?> response = bankIdClient.auth(request);
      return (OrderResponse) response.getBody();
    } catch (FeignException ex) {
      logger.error(
          "Auth - Something went wrong that wasn't mapped with the BankIdRestErrorDecoder. Status: {} , Message: {}",
          ex.status(), ex.getMessage());
      throw new BankIdError(BankIdErrorType.UNKNOWN, String.valueOf(ex.status()),
          ex.getMessage());
    }
  }

  @Override
  public OrderResponse sign(String personalNumber, String endUserIp, String userVisibleData) {
    try {
      ResponseEntity<?> response = bankIdClient.sign(new OrderSignRequest(personalNumber, endUserIp, userVisibleData));
      return (OrderResponse) response.getBody();
    } catch (FeignException ex) {
      logger.error(
          "Sign - Something went wrong that wasn't mapped with the BankIdRestErrorDecoder. Status: {} , Message: {}",
          ex.status(), ex.getMessage());
      throw new BankIdError(BankIdErrorType.UNKNOWN, String.valueOf(ex.status()),
          ex.getMessage());
    }
  }

  @Override
  public CollectResponse collect(CollectRequest request) {
    try {
      ResponseEntity<?> response = bankIdClient.collect(request);
      return (CollectResponse) response.getBody();
    } catch (FeignException ex) {
      logger.error(
          "Collect - Something went wrong that wasn't mapped with the BankIdRestErrorDecoder. Status: {} , Message: {}",
          ex.status(), ex.getMessage());
      throw new BankIdError(BankIdErrorType.UNKNOWN, String.valueOf(ex.status()),
          ex.getMessage());
    }
  }
}
