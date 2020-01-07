package com.hedvig.memberservice.services;

import com.hedvig.external.bankID.bankIdTypes.CollectResponse;
import com.hedvig.external.bankID.bankIdTypes.OrderResponse;
import org.springframework.lang.NonNull;

public interface BankIdRestService {

  OrderResponse startSign(final @NonNull String ssn,
      final @NonNull String userVisibleMessage, final @NonNull String ipAddress);

  CollectResponse collect(@NonNull String someOrderRef);
}
