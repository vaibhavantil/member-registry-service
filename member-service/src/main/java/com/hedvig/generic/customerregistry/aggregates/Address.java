package com.hedvig.generic.customerregistry.aggregates;

import lombok.Value;

import java.util.List;

@Value
public class Address {
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
