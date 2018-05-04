package com.hedvig.memberservice.web.dto;

import lombok.Value;

import java.time.LocalDate;

@Value
public class MemberCancelInsurance {
    LocalDate cancellationDate;
}
