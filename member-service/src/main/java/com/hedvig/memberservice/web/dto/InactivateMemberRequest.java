package com.hedvig.memberservice.web.dto;

import lombok.Value;

import java.time.ZonedDateTime;

@Value
public class InactivateMemberRequest {
    ZonedDateTime inactivationDate;
}
