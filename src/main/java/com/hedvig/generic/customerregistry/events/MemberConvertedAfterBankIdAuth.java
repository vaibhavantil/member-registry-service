package com.hedvig.generic.customerregistry.events;

import com.hedvig.generic.customerregistry.aggregates.MemberStatus;
import lombok.Value;

import java.time.LocalDate;

@Value
public class MemberConvertedAfterBankIdAuth {
    private Long memberId;
    private String personalIdentificationNumber;
    private String givenName;
    private String surname;
    private String name;
    private MemberStatus newStatus;
}
