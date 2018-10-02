package com.hedvig.external.bankID.bankIdRest;

import com.hedvig.external.bankID.bankIdRestTypes.CollectRequest;
import com.hedvig.external.bankID.bankIdRestTypes.CollectResponse;
import com.hedvig.external.bankID.bankIdRestTypes.OrderAuthRequest;
import com.hedvig.external.bankID.bankIdRestTypes.OrderResponse;
import com.hedvig.external.bankID.bankIdRestTypes.OrderSignRequest;

public interface BankIdRestApi {

  OrderResponse auth(OrderAuthRequest request);

  OrderResponse sign(OrderSignRequest request);

  CollectResponse collect(CollectRequest request);
}
