package com.hedvig.memberservice.services.bankid;

import com.hedvig.external.bankID.bankidTypes.CollectResponse;
import com.hedvig.external.bankID.bankidTypes.OrderResponse;

import java.io.UnsupportedEncodingException;

public interface BankIdApi {
    OrderResponse auth(String ssn);
    OrderResponse sign(String ssn, String message) throws UnsupportedEncodingException;
    CollectResponse authCollect(String orderRef);
    CollectResponse signCollect(String orderRef);
}
