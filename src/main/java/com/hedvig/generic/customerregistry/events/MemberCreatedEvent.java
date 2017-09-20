package com.hedvig.generic.customerregistry.events;

import com.hedvig.generic.customerregistry.aggregates.MemberStatus;
import lombok.Value;

import java.time.LocalDate;

@Value
public class MemberCreatedEvent {

    private Long id;

    private MemberStatus status;

}
