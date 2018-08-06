package com.hedvig.memberservice.query;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import lombok.Getter;
import lombok.Setter;

@Entity
public class SignedMemberEntity {
  @Id @Getter @Setter private Long id;

  @Getter
  @Setter
  @Column(unique = true)
  private String ssn;
}
