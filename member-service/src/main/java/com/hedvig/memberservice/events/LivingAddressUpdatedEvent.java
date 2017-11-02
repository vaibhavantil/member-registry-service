package com.hedvig.memberservice.events;

import com.hedvig.memberservice.aggregates.BisnodeAddress;
import lombok.Value;

import java.util.Optional;

@Value
public class LivingAddressUpdatedEvent {
    private final Long id;
    private final String street;
    private final String city;
    private final String zipCode;
    private final String apartmentNo;

    public LivingAddressUpdatedEvent(Long id, String street, String city, String zipCode, String apartmentNo) {
        this.id = id;
        this.street = street;
        this.city = city;
        this.zipCode = zipCode;
        this.apartmentNo = apartmentNo;
    }

    public LivingAddressUpdatedEvent(Long memberId, BisnodeAddress a) {
        this.id = memberId;
        String street = orEmpty(a.getStreetName());
        String streetNumber = orEmpty(a.getStreetNumber());
        String entrance = orEmpty(a.getEntrance());

        this.street = String.format("%s %s%s", street, streetNumber, entrance);
        this.city = a.getCity();
        this.zipCode = a.getPostalCode();
        this.apartmentNo = a.getApartment();
    }

    private String orEmpty(String entrance) {
        return Optional.ofNullable(entrance).orElse("");
    }
}
