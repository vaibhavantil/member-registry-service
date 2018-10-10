package com.hedvig.external.bankID.bankIdRest;

import com.hedvig.external.bankID.bankIdRestTypes.CollectRequest;
import com.hedvig.external.bankID.bankIdRestTypes.CollectResponse;
import com.hedvig.external.bankID.bankIdRestTypes.OrderAuthRequest;
import com.hedvig.external.bankID.bankIdRestTypes.OrderResponse;
import javax.validation.constraints.NotNull;

public interface BankIdRestApi {

  OrderResponse auth(OrderAuthRequest request);

  OrderResponse sign(@NotNull String personalNumber, @NotNull String endUserIp,  @NotNull String userVisibleData);

  CollectResponse collect(CollectRequest request);
}
