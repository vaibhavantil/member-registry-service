package com.hedvig.memberservice.query;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class SignedMemberEntity {
  @Id
  private Long id;

  @Column(unique = true)
  private String ssn;

  public Long getId() {
    return this.id;
  }

  public String getSsn() {
    return this.ssn;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public void setSsn(String ssn) {
    this.ssn = ssn;
  }
}
