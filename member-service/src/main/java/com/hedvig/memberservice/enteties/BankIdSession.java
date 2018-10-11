package com.hedvig.memberservice.enteties;

import com.hedvig.external.bankID.bankIdRestTypes.CollectStatus;
import java.time.Instant;
import javax.persistence.Embeddable;
import javax.persistence.Embedded;
import javax.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;

@Embeddable
@Data
public class BankIdSession {

  @NotNull
  String autoStartToken;

  @NotNull
  String orderReference;

  @Setter(AccessLevel.NONE)
  Instant createdAt;

  @Setter(AccessLevel.NONE)
  Instant updatedAt;

  @Embedded
  @Setter(AccessLevel.NONE)
  CollectResponse collectResponse;

  public BankIdSession(){
  }

  BankIdSession(@NotNull String autoStartToken, @NotNull String orderReference) {
    this.createdAt = Instant.now();
    this.autoStartToken = autoStartToken;
    this.orderReference = orderReference;
  }

  boolean canBeReused() {

    if (collectResponse != null) {
      return collectResponse.getStatus() == CollectStatus.pending;
    }

    return true;
  }

  void newCollectResponse(CollectResponse collectResponse) {
    this.collectResponse = collectResponse;
    updatedAt = Instant.now();
  }
}
