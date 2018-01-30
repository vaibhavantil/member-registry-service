package com.hedvig.memberservice.externalApi.prouctsPricing.dto;

import lombok.Value;

import java.util.List;

@Value
public class InsuranceStatusDTO {

    List<String> safetyIncreasers;
    String insuranceStatus;

}
