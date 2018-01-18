package com.hedvig.memberservice.services.bankid;

import com.hedvig.external.bankID.bankidTypes.CollectResponse;
import com.hedvig.external.bankID.bankidTypes.OrderResponse;
import com.hedvig.external.bankID.bankidTypes.ProgressStatus;
import com.hedvig.external.bankID.bankidTypes.UserInfo;
import com.hedvig.external.billectaAPI.BillectaApi;
import com.hedvig.external.billectaAPI.api.BankIdAuthenticationStatus;
import com.hedvig.external.billectaAPI.api.BankIdSignStatus;
import org.springframework.stereotype.Component;

@Component
public class BillectaAdapter implements BankIdApi {

    private final BillectaApi api;

    BillectaAdapter(BillectaApi api) {

        this.api = api;
    }

    public OrderResponse auth(String ssn) {
        BankIdAuthenticationStatus status = api.BankIdAuth(ssn);

        return new OrderResponse(status.getReferenceToken(), status.getAutoStartToken());
    }

    public OrderResponse sign(String ssn, String message) {
        BankIdSignStatus status = api.BankIdSign(ssn, message);

        return new OrderResponse(status.getReferenceToken(), status.getAutoStartToken());
    }

    public CollectResponse authCollect(String orderRef) {
        BankIdAuthenticationStatus status = api.BankIdCollect(orderRef);
        return new CollectResponse(
                ProgressStatus.valueOf(status.getStatus().name()),
                "",
                new UserInfo(status.getName(), status.getGivenName(), status.getSurname(), status.getSSN(), status.getIpAddress(), status.getNotBefore(), status.getNotAfter()),
                "");
    }

    public CollectResponse signCollect(String orderRef) {
        BankIdSignStatus status = api.bankIdSignCollect(orderRef);
        return new CollectResponse(
                ProgressStatus.valueOf(status.getStatus().name()),
                "",
                new UserInfo(status.getName(), status.getGivenName(), status.getSurname(), status.getSSN(), status.getIpAddress(), status.getNotBefore(), status.getNotAfter()),
                "");
    }


}
