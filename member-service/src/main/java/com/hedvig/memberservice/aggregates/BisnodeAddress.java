package com.hedvig.memberservice.aggregates;

import java.util.List;
import lombok.Value;

@Value
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
}
