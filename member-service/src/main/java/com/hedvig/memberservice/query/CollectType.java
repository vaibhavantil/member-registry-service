package com.hedvig.memberservice.query;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;

@Entity
public class CollectType {

  @Id public String token;
  @Enumerated(EnumType.STRING)
  public RequestType type;
  public Long memberId;

  public enum RequestType {
    AUTH,
    RETRIEVE_ACCOUNTS,
    SIGN
  }
}
