package com.hedvig.memberservice.web.dto;

import lombok.Value;

@Value
public class BankAccountDetails {

    private final Long amount;
    private String name;
    private String clearingNumber;
    private String number;

    public BankAccountDetails(String name, String clearingNumber, String number, Long amount) {
        this.name = name;
        this.clearingNumber = clearingNumber;
        this.number = number;
        this.amount = amount;
    }
}
