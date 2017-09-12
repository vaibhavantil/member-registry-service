package com.hedvig.generic.mustrename.query;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.time.LocalDate;

@Entity
public class UserEntity {

    @Id
    public String id;

    public String name;

    public LocalDate birthDate;


}
