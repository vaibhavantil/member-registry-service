package com.hedvig.memberservice.externalApi.productsPricing.dto;

import lombok.Value;

import java.time.ZonedDateTime;

@Value
public class SetCancellationDateRequest {
    ZonedDateTime inactivationDate;
}
