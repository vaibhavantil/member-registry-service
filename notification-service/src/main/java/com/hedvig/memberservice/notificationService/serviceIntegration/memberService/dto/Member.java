package com.hedvig.memberservice.notificationService.serviceIntegration.memberService.dto;

import lombok.AllArgsConstructor;
import lombok.Value;

import java.time.LocalDate;

@Value
@AllArgsConstructor
public class Member {

    private final Long memberId;
    private final String ssn;

    private final String firstName;
    private final String lastName;


    private final String street;
    private final String city;
    private final String zipCode;
    private final Integer floor;

    private final String email;
    private final String phoneNumber;
    private final String country;

    private final LocalDate birthDate;
    private final String apartment;

}
