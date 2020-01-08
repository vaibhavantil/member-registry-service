package com.hedvig.external.bankID.bankId;

import com.hedvig.external.bankID.bankIdTypes.CollectRequest;
import com.hedvig.external.bankID.bankIdTypes.CollectResponse;
import com.hedvig.external.bankID.bankIdTypes.OrderAuthRequest;
import com.hedvig.external.bankID.bankIdTypes.OrderResponse;
import javax.validation.constraints.NotNull;

public interface BankIdApi {

  OrderResponse auth(OrderAuthRequest request);

  OrderResponse sign(@NotNull String personalNumber, @NotNull String endUserIp,  @NotNull String userVisibleData);

  CollectResponse collect(CollectRequest request);
}
