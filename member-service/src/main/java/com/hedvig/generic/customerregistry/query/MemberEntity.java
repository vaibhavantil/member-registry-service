package com.hedvig.generic.customerregistry.query;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDate;

@Entity
public class MemberEntity {

    @Id
    @Getter
    @Setter
    private Long id;

    @Getter
    @Setter
    private String status;

    @Getter
    @Setter
    @Column(unique = true)
    private String ssn;

    @Getter
    @Setter
    private String preferredName;
    
    @Getter
    @Setter
    private String fullName;

    @Getter
    @Setter
    private LocalDate birthDate;

    @Getter
    @Setter
    private String streetName;

    @Getter
    @Setter
    private String streetNumber;

    @Getter
    @Setter
    private String entrance;

    @Getter
    @Setter String postalCode;

    @Getter
    @Setter
    private String city;

    @Getter
    @Setter
    private String phoneNumber;

}
