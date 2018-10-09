package com.hedvig.memberservice.enteties;

import com.hedvig.external.bankID.bankIdRestTypes.CollectStatus;
import javax.persistence.Embeddable;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import lombok.Data;

@Embeddable
@Data
public class CollectResponse {

  @Enumerated(EnumType.STRING)
  CollectStatus status;

  private String hintCode;
}
