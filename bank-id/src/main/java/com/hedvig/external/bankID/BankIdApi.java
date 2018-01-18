package com.hedvig.external.bankID;

import bankid.CollectResponseType;
import bankid.OrderResponseType;
import com.hedvig.external.bankID.bankidTypes.CollectResponse;
import com.hedvig.external.bankID.bankidTypes.OrderResponse;
import com.hedvig.external.bankID.bankidTypes.ProgressStatus;
import com.hedvig.external.bankID.bankidTypes.UserInfo;
import org.springframework.stereotype.Component;

import java.io.UnsupportedEncodingException;

@Component
public class BankIdApi {


    private final BankIdClient bankIdClient;

    public BankIdApi(BankIdClient bankIdClient) {

        this.bankIdClient = bankIdClient;
    }

    public OrderResponse auth() {
        OrderResponseType responseType = bankIdClient.auth(null);
        return new OrderResponse(responseType.getOrderRef(), responseType.getAutoStartToken());
    }

    public OrderResponse sign(String ssn, String message) throws UnsupportedEncodingException {
        OrderResponseType responseType = bankIdClient.sign(ssn, message);
        return new OrderResponse(responseType.getOrderRef(), responseType.getAutoStartToken());
    }

    public CollectResponse collect(String orderReference) {
        CollectResponseType responseType = bankIdClient.collect(orderReference);

        UserInfo userInfo = null;
        if(responseType.getUserInfo() != null) {
            userInfo = new UserInfo(responseType.getUserInfo());
        }

        return new CollectResponse(
                ProgressStatus.valueOf(responseType.getProgressStatus().name()),
                responseType.getSignature(),
                userInfo,
                responseType.getOcspResponse());
    }
}
