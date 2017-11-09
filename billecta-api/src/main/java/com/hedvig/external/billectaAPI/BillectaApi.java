package com.hedvig.external.billectaAPI;

import com.hedvig.external.billectaAPI.api.BankAccountRequest;
import com.hedvig.external.billectaAPI.api.BankIdAuthenticationStatus;
import com.hedvig.external.billectaAPI.api.BankIdSignStatus;
import org.springframework.http.ResponseEntity;

import java.util.function.Consumer;

public interface BillectaApi {
    BankIdAuthenticationStatus BankIdAuth(String ssn);

    BankIdAuthenticationStatus BankIdCollect(String token);

    String initBankAccountRetreivals(String ssn, String bankId);

    ResponseEntity<BankAccountRequest> getBankAccountNumbers(String publicId);

    String retrieveBankAccountNumbers(
            String ssn,
            String bankId,
            Consumer<BankAccountRequest> onComplete,
            Consumer<String> onError);

    BankIdSignStatus BankIdSign(String ssn, String usermessage);

    BankIdSignStatus bankIdSignCollect(String referenceToken);
}
