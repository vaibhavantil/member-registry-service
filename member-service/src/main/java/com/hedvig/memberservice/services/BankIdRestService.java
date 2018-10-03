package com.hedvig.memberservice.services;

import com.hedvig.external.bankID.bankIdRestTypes.OrderResponse;

public interface BankIdRestService {

  OrderResponse startSign(long memberId, String ssn, String userVisibleMessage);
}
