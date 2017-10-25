package com.hedvig.memberservice.web.dto.events;

import com.hedvig.external.billectaAPI.api.BankAccount;
import com.hedvig.external.billectaAPI.api.BankAccountRequest;
import lombok.Value;

import java.util.ArrayList;
import java.util.List;

@Value
public class BankAccountRetrievalSuccess extends MemberServiceEventPayload {
    private List<BankAccountDetails> accounts;

    public BankAccountRetrievalSuccess(List<BankAccountDetails> as) {
        this.accounts = as;
    }

    public BankAccountRetrievalSuccess(BankAccountRequest accounts) {
        this.accounts = new ArrayList<>();

        for (BankAccount acc: accounts.getAccountNumbers().getBankAccount()) {

            BankAccountDetails bad = new BankAccountDetails(
                    acc.getType(),
                    acc.getNumber().split(",")[0],
                    formatAccount(acc.getNumber().split(",")[1]),
                    acc.getBalance().getValue());

            this.accounts.add(bad);
        }
    }

    private String formatAccount(String account) {
        int length = account.length();
        int start = Integer.max(0, length-5);
        int end = Integer.min(3, length);
        return String.format("%s..%s", account.substring(0, end), account.subSequence(start, length));
    }

}
