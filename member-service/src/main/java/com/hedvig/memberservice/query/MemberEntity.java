package com.hedvig.memberservice.query;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.Objects;

@Entity
public class MemberEntity {

    @Id
    @Getter
    @Setter
    private Long id;

    @Getter
    @Setter
    private String apartment;

    @Getter
    @Setter
    private String status;

    @Getter
    @Setter
    @Column(unique = true)
    private String ssn;

    @Getter
    @Setter
    private String firstName;

    @Getter
    @Setter
    private String lastName;


    @Getter
    @Setter
    private LocalDate birthDate;

    @Getter
    @Setter
    private String street;


    @Getter
    @Setter String zipCode;

    @Getter
    @Setter
    private String city;

    @Getter
    @Setter
    private String phoneNumber;

    @Getter
    @Setter
    private String email;
}
