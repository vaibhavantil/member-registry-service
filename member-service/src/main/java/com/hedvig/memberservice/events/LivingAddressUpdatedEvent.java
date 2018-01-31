package com.hedvig.memberservice.events;

import com.hedvig.memberservice.aggregates.BisnodeAddress;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Value
@Slf4j
public class LivingAddressUpdatedEvent {
    private final Long id;
    private final String street;
    private final String city;
    private final String zipCode;
    private final String apartmentNo;
    private final int floor;

    public LivingAddressUpdatedEvent(Long id, String street, String city, String zipCode, String apartmentNo, int floor) {
        this.id = id;
        this.street = street;
        this.city = city;
        this.zipCode = zipCode;
        this.apartmentNo = apartmentNo;
        this.floor = floor;
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
        this.floor = parseFloorFromApartment(a.getApartment());
    }

    private int parseFloorFromApartment(String apartmentNo) {
        final Pattern compile = Pattern.compile("\\d\\d\\d\\d");
        final Matcher matcher = compile.matcher(apartmentNo == null ? "": apartmentNo);
        if(matcher.matches()){
            try {
                return Integer.parseInt(apartmentNo.substring(0, 2)) - 10;
            }catch (NumberFormatException ex) {
                log.error("Could not parse apartmentnumber: " + ex.getMessage(), ex);
            }
        }
        log.error("ApartmentNo does not match regex. apartmentNo: '{}'", apartmentNo);

        return 0;
    }

    private String orEmpty(String entrance) {
        return Optional.ofNullable(entrance).orElse("");
    }
}
