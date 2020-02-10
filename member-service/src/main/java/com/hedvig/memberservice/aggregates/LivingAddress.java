package com.hedvig.memberservice.aggregates;

import java.util.Objects;
import lombok.Value;

@Value
public class LivingAddress {

  /** Represents Street, number, entrance */
  String street;

  String city;
  String zip;
  String apartmentNo;
  Integer floor;

  public boolean needsUpdate(
      String street, String city, String zipCode, String apartmentNo, Integer floor) {
    if (street == null && city == null && zipCode == null && apartmentNo == null && floor == null) {
      return false;
    }
    return !Objects.equals(this.street, street)
        || !Objects.equals(this.city, city)
        || !Objects.equals(this.zip, zipCode)
        || !Objects.equals(this.apartmentNo, apartmentNo)
        || !Objects.equals(this.floor, floor);
  }
}
