package com.hedvig.memberservice.services.bankid;

import com.hedvig.external.bankID.bankIdRestTypes.CollectResponse;
import com.hedvig.external.bankID.bankIdRestTypes.OrderResponse;
import java.io.UnsupportedEncodingException;

public interface BankIdSOAPApi {
  OrderResponse auth();

  OrderResponse sign(String ssn, String message) throws UnsupportedEncodingException;

  CollectResponse authCollect(String orderRef);

  CollectResponse signCollect(String orderRef);
}
