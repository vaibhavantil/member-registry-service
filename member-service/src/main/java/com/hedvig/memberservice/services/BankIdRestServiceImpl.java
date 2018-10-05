package com.hedvig.memberservice.services;

import com.hedvig.external.bankID.bankIdRest.BankIdRestApi;
import com.hedvig.external.bankID.bankIdRestTypes.CollectRequest;
import com.hedvig.external.bankID.bankIdRestTypes.CollectResponse;
import com.hedvig.external.bankID.bankIdRestTypes.OrderResponse;
import java.util.Base64;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

@Service
public class BankIdRestServiceImpl implements BankIdRestService {

  private final BankIdRestApi bankIdRestApi;


  public BankIdRestServiceImpl(BankIdRestApi bankIdRestApi) {
    this.bankIdRestApi = bankIdRestApi;
  }

  @Override
  public OrderResponse startSign(long memberId, @NonNull final String ssn, @NonNull final String userVisibleMessage, @NonNull final String ipNumber) {
    return bankIdRestApi.sign(ssn, ipNumber, Base64.getEncoder().encodeToString(userVisibleMessage.getBytes()));
  }

  @Override
  public CollectResponse collect(@NonNull final String orderRef) {

    return bankIdRestApi.collect(new CollectRequest(orderRef));
  }
}
