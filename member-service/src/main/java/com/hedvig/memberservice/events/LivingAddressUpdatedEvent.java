package com.hedvig.memberservice.events;

import com.hedvig.memberservice.aggregates.BisnodeAddress;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static com.hedvig.memberservice.aggregates.BisnodeAddress.parseFloorFromApartment;

@Value
@Slf4j
public class LivingAddressUpdatedEvent implements Traceable {
  private final Long id;
  private final String street;
  private final String city;
  private final String zipCode;
  private final String apartmentNo;
  private final int floor;

  public LivingAddressUpdatedEvent(
    Long id, String street, String city, String zipCode, String apartmentNo, int floor) {
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

  private String orEmpty(String entrance) {
    return Optional.ofNullable(entrance).orElse("");
  }

  @Override
  public Long getMemberId() {
    return id;
  }

  @Override
  public Map<String, Object> getValues() {
    Map result = new HashMap();
    result.put("Street", street);
    result.put("City", city);
    result.put("Zip code", zipCode);
    result.put("Apartment No", apartmentNo);
    result.put("Floor", floor);
    return result;
  }
}
