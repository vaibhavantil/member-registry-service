package com.hedvig.memberservice.commands;

import lombok.Value;

import java.time.ZonedDateTime;

@Value
public class MemberCancelInsuranceCommand {
    Long memberId;
    ZonedDateTime inactivationDate;
}
