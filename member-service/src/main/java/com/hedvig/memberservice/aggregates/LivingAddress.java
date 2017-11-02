package com.hedvig.memberservice.aggregates;

import lombok.Value;

@Value
public class LivingAddress {

    /**
     * Represents Street, number, entrance
     */
    String street;
    String city;
    String zip;

}
