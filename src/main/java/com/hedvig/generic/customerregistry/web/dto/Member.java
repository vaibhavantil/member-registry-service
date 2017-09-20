package com.hedvig.generic.customerregistry.web.dto;

import lombok.Value;

@Value
public class Member {

    private final String prefferedName;
    private final String fullName;


    private final String Street;
    private final String City;
    private final String zipCode;


    private final String hedvigId;
    private final String email;
    private final String phoneNumber;
    private final String country;
}
