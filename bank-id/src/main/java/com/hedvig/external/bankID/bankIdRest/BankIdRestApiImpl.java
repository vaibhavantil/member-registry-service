package com.hedvig.external.bankID.bankIdRest;

import com.hedvig.external.bankID.bankIdRestTypes.CollectRequest;
import com.hedvig.external.bankID.bankIdRestTypes.CollectResponse;
import com.hedvig.external.bankID.bankIdRestTypes.OrderAuthRequest;
import com.hedvig.external.bankID.bankIdRestTypes.OrderResponse;
import com.hedvig.external.bankID.bankIdRestTypes.OrderSignRequest;
import org.springframework.stereotype.Component;

@Component
public class BankIdRestApiImpl implements BankIdRestApi {

  private final BankIdRestClient bankIdRestClient;

  public BankIdRestApiImpl(BankIdRestClient bankIdRestClient) {
    this.bankIdRestClient = bankIdRestClient;
  }

  @Override
  public OrderResponse auth(OrderAuthRequest request) {
    return bankIdRestClient.auth(request).getBody();
  }

  @Override
  public OrderResponse sign(OrderSignRequest request) {
    return bankIdRestClient.sign(request).getBody();
  }

  @Override
  public CollectResponse collect(CollectRequest request) {
    return bankIdRestClient.collect(request).getBody();
  }
}
