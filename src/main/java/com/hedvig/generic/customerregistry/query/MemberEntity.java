package com.hedvig.generic.customerregistry.query;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.time.LocalDate;

@Entity
public class MemberEntity {

    @Id
    @Getter
    @Setter
    private String id;

    @Getter
    @Setter
    private String personalIdentificationNumber;


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
    private String street;


}
