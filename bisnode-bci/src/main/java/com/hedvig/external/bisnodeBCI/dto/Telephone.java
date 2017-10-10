package com.hedvig.external.bisnodeBCI.dto;

import lombok.Value;

@Value
public class Telephone {
    private String type;
    private String number;
    private Boolean telemarketingRestriction;
    private Boolean secretPhoneNumber;
}
