package com.hedvig.memberservice.web.dto;

import com.hedvig.external.billectaAPI.api.BankAccount;
import com.hedvig.external.billectaAPI.api.BankAccountRequest;
import lombok.Value;

import java.util.ArrayList;
import java.util.List;

@Value
public class BankAccountDetailsList {
    private List<BankAccountDetails> accounts;

    public BankAccountDetailsList(List<BankAccountDetails> as) {
        this.accounts = as;
    }

    public BankAccountDetailsList(BankAccountRequest accounts) {
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
