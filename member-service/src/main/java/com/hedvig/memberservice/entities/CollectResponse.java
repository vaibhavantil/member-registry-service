package com.hedvig.memberservice.entities;

import com.hedvig.external.bankID.bankIdTypes.CollectStatus;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import lombok.Data;

@Embeddable
public class CollectResponse {

  public CollectResponse(
    CollectStatus status,
    String hintCode
  ) {
    this.status = status;
    this.hintCode = hintCode;
  }

  @Enumerated(EnumType.STRING)
  @Column(name = "collect_response_status")
  CollectStatus status;

  @Column(name = "collect_reponse_hint_code")
  private String hintCode;

  public CollectStatus getStatus() {
    return status;
  }

  public String getHintCode() {
    return hintCode;
  }
}
