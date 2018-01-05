package com.hedvig.memberservice.services.bankid;

import com.hedvig.external.bankID.bankidTypes.CollectResponse;
import com.hedvig.external.bankID.bankidTypes.OrderResponse;

import java.io.UnsupportedEncodingException;

public class BankIdAdapter implements BankIdApi {

    private final com.hedvig.external.bankID.BankIdApi api;

    public BankIdAdapter(com.hedvig.external.bankID.BankIdApi api) {

        this.api = api;
    }

    @Override
    public OrderResponse auth(String ssn) {
        return api.auth();
    }

    @Override
    public OrderResponse sign(String ssn, String message) throws UnsupportedEncodingException {
        return api.sign(ssn, message);
    }

    @Override
    public CollectResponse authCollect(String orderRef) {
        return api.collect(orderRef);
    }

    @Override
    public CollectResponse signCollect(String orderRef) {
        return api.collect(orderRef);
    }
}
