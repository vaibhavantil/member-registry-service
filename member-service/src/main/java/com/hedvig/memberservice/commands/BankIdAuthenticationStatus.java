package com.hedvig.memberservice.commands;

import lombok.Data;
import lombok.Value;

@Data
public class BankIdAuthenticationStatus {

    String SSN;

    String referenceToken;

    String surname;
    String givenName;



}
