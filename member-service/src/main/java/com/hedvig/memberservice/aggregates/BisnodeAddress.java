package com.hedvig.memberservice.aggregates;

import lombok.Value;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Value
@Slf4j
public class BisnodeAddress {

  private String type;
  private String careOf;
  private String streetName;
  private String streetNumber;
  private String entrance;
  private String apartment;
  private String floor;
  private String postOfficeBox;
  private String postalCode;
  private String city;
  private String country;
  private List<String> formattedAddress;

    public static int parseFloorFromApartment(String apartmentNo) {

      if(apartmentNo == null){
        return 0;
      }

      final Pattern compile = Pattern.compile("\\d\\d\\d\\d");
      final Matcher matcher = compile.matcher(apartmentNo);
      if (matcher.matches()) {
        try {
          return Integer.parseInt(apartmentNo.substring(0, 2)) - 10;
        } catch (NumberFormatException ex) {
          log.error("Could not parse apartmentnumber: " + ex.getMessage(), ex);
        }
      }
      log.error("ApartmentNo does not match regex. apartmentNo: '{}'", apartmentNo);

      return 0;
    }
}
