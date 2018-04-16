package com.hedvig.memberservice.services;

import com.hedvig.external.bankID.bankidTypes.CollectResponse;
import com.hedvig.external.bankID.bankidTypes.OrderResponse;
import com.hedvig.memberservice.query.CollectRepository;
import com.hedvig.memberservice.query.CollectType;
import com.hedvig.memberservice.services.bankid.BankIdApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;

import static com.hedvig.memberservice.query.CollectType.RequestType;

@Service
public class BankIdService {

    private final BankIdApi bankIdApi;
    private final CollectRepository collectRepository;
    private final Logger log = LoggerFactory.getLogger(BankIdService.class);

    public BankIdService(BankIdApi bankIdApi, CollectRepository collectRepository) {
        this.bankIdApi = bankIdApi;
        this.collectRepository = collectRepository;
    }

    public OrderResponse auth(String ssn, Long memberId) {
        OrderResponse status = bankIdApi.auth(ssn);
        log.info("Started bankId AUTH autostart:{}, reference:{}, ssn:{}",
                status.getAutoStartToken(),
                status.getOrderRef(),
                ssn);
        trackAuthToken(status.getOrderRef(), memberId);
        return status;
    }

    public OrderResponse sign(String ssn, String userMessage, Long memberId) throws UnsupportedEncodingException {
        OrderResponse status = bankIdApi.sign(ssn, userMessage);
        trackSignToken(status.getOrderRef(), memberId);
        return status;
    }

    private void trackSignToken(String referenceToken, Long memberId) {
        trackReferenceToken(referenceToken, RequestType.SIGN, memberId);
    }

    private void trackAuthToken(String referenceToken, Long memberId) {
        trackReferenceToken(referenceToken, RequestType.AUTH, memberId);
    }

    private void trackReferenceToken(String referenceToken, RequestType sign, Long memberId) {
        CollectType ct = new CollectType();
        ct.token = referenceToken;
        ct.type = sign;
        ct.memberId = memberId;
        collectRepository.save(ct);
    }

    public CollectResponse authCollect(String referenceToken) {
        return bankIdApi.authCollect(referenceToken);
    }

    public CollectResponse signCollect(String referenceToken) {
        return bankIdApi.signCollect(referenceToken);
    }
}
