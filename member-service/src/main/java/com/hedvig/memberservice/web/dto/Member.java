package com.hedvig.memberservice.web.dto;

import com.hedvig.memberservice.query.MemberEntity;
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

    public Member(MemberEntity memberEntity) {
        this.firstName = memberEntity.getFirstName();
        this.lastName = memberEntity.getLastName();
        this.memberId = memberEntity.getId();
        this.ssn = memberEntity.getSsn();
        this.street = memberEntity.getStreet();
        this.zipCode = memberEntity.getZipCode();
        this.floor = memberEntity.getFloor() == null ? 0: memberEntity.getFloor();
        this.city = memberEntity.getCity();
        this.apartment = memberEntity.getApartment();
        this.email = memberEntity.getEmail();
        this.phoneNumber = memberEntity.getPhoneNumber();
        this.country = "SE";
        this.birthDate = memberEntity.getBirthDate();
    }
}
