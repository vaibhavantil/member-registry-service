package com.hedvig.memberservice.commands;

import lombok.Value;
import org.axonframework.commandhandling.TargetAggregateIdentifier;

import java.time.LocalDate;

@Value
public class MemberCancelInsuranceCommand {
    @TargetAggregateIdentifier
    Long memberId;

    LocalDate inactivationDate;
}
