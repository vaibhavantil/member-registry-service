package com.hedvig.memberservice.services;

import com.hedvig.external.bankID.bankId.BankIdApi;
import com.hedvig.external.bankID.bankIdTypes.CollectRequest;
import com.hedvig.external.bankID.bankIdTypes.CollectResponse;
import com.hedvig.external.bankID.bankIdTypes.OrderResponse;
import java.util.Base64;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

@Service
public class BankIdRestServiceImpl implements BankIdRestService {

  private final BankIdApi bankIdApi;


  public BankIdRestServiceImpl(BankIdApi bankIdApi) {
    this.bankIdApi = bankIdApi;
  }

  @Override
  public OrderResponse startSign(@NonNull final String ssn,
      @NonNull final String userVisibleMessage, @NonNull final String ipNumber) {
    return bankIdApi.sign(ssn, ipNumber, Base64.getEncoder().encodeToString(userVisibleMessage.getBytes()));
  }

  @Override
  public CollectResponse collect(@NonNull final String orderRef) {

    return bankIdApi.collect(new CollectRequest(orderRef));
  }
}
