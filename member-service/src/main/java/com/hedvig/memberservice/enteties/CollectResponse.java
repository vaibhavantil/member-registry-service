package com.hedvig.memberservice.enteties;

import com.hedvig.external.bankID.bankIdRestTypes.CollectStatus;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.validation.constraints.NotNull;
import lombok.Data;

@Embeddable
@Data
public class CollectResponse {

  @NotNull
  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  CollectStatus status;

  private String hintCode;
}
