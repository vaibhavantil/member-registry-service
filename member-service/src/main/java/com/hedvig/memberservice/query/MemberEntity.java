package com.hedvig.memberservice.query;

import java.time.Instant;
import java.time.LocalDate;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(
    name = "member_entity",
    indexes = {@Index(columnList = "ssn", name = "ix_member_entity_ssn")})
public class MemberEntity {

  @Getter @Setter String zipCode;
  @Id @Getter @Setter private Long id;
  @Getter @Setter private String apartment;
  @Getter @Setter private String status;
  @Getter @Setter private String ssn;
  @Getter @Setter private String firstName;
  @Getter @Setter private String lastName;
  @Getter @Setter private LocalDate birthDate;
  @Getter @Setter private String street;
  @Getter @Setter private String city;

  @Getter @Setter private String phoneNumber;

  @Getter @Setter private String email;

  @Getter @Setter private String cashbackId;

  @Getter @Setter private Integer floor;

  @Getter @Setter private Instant signedOn;

  @Getter @Setter private Instant createdOn;
}
