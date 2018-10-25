package com.hedvig.memberservice.entities;

import com.hedvig.external.bankID.bankIdRestTypes.CollectStatus;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import lombok.Data;

@Embeddable
@Data
public class CollectResponse {

  @Enumerated(EnumType.STRING)
  @Column(name = "collect_response_status")
  CollectStatus status;

  @Column(name = "collect_reponse_hint_code")
  private String hintCode;
}
