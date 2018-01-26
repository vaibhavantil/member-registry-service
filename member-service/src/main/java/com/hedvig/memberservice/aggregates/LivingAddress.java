package com.hedvig.memberservice.aggregates;

import lombok.Value;

import java.util.Objects;

@Value
public class LivingAddress {

    /**
     * Represents Street, number, entrance
     */
    String street;
    String city;
    String zip;
    String apartmentNo;
    Integer floor;

    public boolean needsUpdate(String street, String city, String zipCode, String apartmentNo, Integer floor) {
        return  !Objects.equals(this.street, street) ||
                !Objects.equals(this.city, city) ||
                !Objects.equals(this.zip, zipCode) ||
                !Objects.equals(this.apartmentNo, apartmentNo) ||
                !Objects.equals(this.floor, floor);
    }
}
