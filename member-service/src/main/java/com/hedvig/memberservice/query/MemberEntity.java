package com.hedvig.memberservice.query;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;
import java.time.LocalDate;

@Entity
@Table(name="member_entity", indexes={
        @Index(columnList = "ssn", name="ix_member_entity_ssn")
})

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

    @Getter
    @Setter
    private String cashbackId;

    @Getter
    @Setter
    private Integer floor;

}
