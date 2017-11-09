package com.hedvig.external.billectaAPI;

import com.hedvig.external.billectaAPI.api.*;
import org.springframework.http.ResponseEntity;

import java.util.UUID;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class BillectaApiFake implements BillectaApi {

    public static final String SSN = "";
    public static final String BANKD_ACCOUNT_GUID = "bankdAccountGuid";

    @Override
    public BankIdAuthenticationStatus BankIdAuth(String ssn) {
        BankIdAuthenticationStatus bankIdAuthenticationStatus = new BankIdAuthenticationStatus();
        bankIdAuthenticationStatus.setStatus(BankIdStatusType.STARTED);
        bankIdAuthenticationStatus.setSSN(SSN);
        bankIdAuthenticationStatus.setAutoStartToken(UUID.randomUUID().toString());
        bankIdAuthenticationStatus.setReferenceToken(UUID.randomUUID().toString());
        return bankIdAuthenticationStatus;
    }

    @Override
    public BankIdAuthenticationStatus BankIdCollect(String token) {
        BankIdAuthenticationStatus bankIdAuthenticationStatus = new BankIdAuthenticationStatus();
        bankIdAuthenticationStatus.setStatus(BankIdStatusType.COMPLETE);
        bankIdAuthenticationStatus.setSSN(SSN);
        bankIdAuthenticationStatus.setAutoStartToken(UUID.randomUUID().toString());
        bankIdAuthenticationStatus.setReferenceToken(token);
        return bankIdAuthenticationStatus;
    }

    @Override
    public String initBankAccountRetreivals(String ssn, String bankId) {
        return BANKD_ACCOUNT_GUID;
    }

    @Override
    public ResponseEntity<BankAccountRequest> getBankAccountNumbers(String publicId) {
        BankAccountRequest body = new BankAccountRequest();
        body.setSSN(SSN);
        body.setBank(BankAccountBankType.FSPA);
        ArrayOfBankAccount accounts = new ArrayOfBankAccount();

        BankAccount account1 = new BankAccount();
        Amount amount = new Amount();
        amount.setValue(1000000);
        amount.setCurrencyCode("SEK");

        account1.setBalance(amount);
        account1.setNumber("1234-132,3");
        account1.setHolderName("Mr X");
        account1.setType("Kortkonto");

        accounts.getBankAccount().add(account1);
        body.setAccountNumbers(accounts);

        body.setPublicId(BANKD_ACCOUNT_GUID);
        return ResponseEntity.ok(body);
    }

    @Override
    public String retrieveBankAccountNumbers(String ssn, String bankId, Consumer<BankAccountRequest> onComplete, Consumer<String> onError) {
        String publicId = this.initBankAccountRetreivals(ssn, bankId);
        BankAccountPoller poller = new BankAccountPoller(publicId, this, new ScheduledThreadPoolExecutor(1), onComplete, onError);
        poller.run();

        return publicId;
    }
    @Override
    public BankIdSignStatus BankIdSign(String ssn, String usermessage) {
        BankIdSignStatus bs = new BankIdSignStatus();
        bs.setAutoStartToken(UUID.randomUUID().toString());
        bs.setReferenceToken(UUID.randomUUID().toString());
        bs.setStatus(BankIdStatusType.STARTED);
        return bs;
    }

    @Override
    public BankIdSignStatus bankIdSignCollect(String referenceToken) {
        BankIdSignStatus bs = new BankIdSignStatus();
        bs.setAutoStartToken(UUID.randomUUID().toString());
        bs.setReferenceToken(referenceToken);
        bs.setStatus(BankIdStatusType.STARTED);
        return bs;
    }
}
