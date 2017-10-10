package com.hedvig.generic.customerregistry.events;

import com.hedvig.generic.customerregistry.aggregates.MemberStatus;
import com.hedvig.generic.customerregistry.aggregates.PersonInformation;
import lombok.Value;

import java.time.LocalDate;

@Value
public class MemberStartedOnBoarding {
    private Long memberId;
    private MemberStatus newStatus;
    private PersonInformation personInformation;
}
