package com.hedvig.memberservice.enteties;

import static javax.persistence.GenerationType.SEQUENCE;

import com.hedvig.external.bankID.bankIdRestTypes.OrderResponse;
import java.time.Instant;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Data
@EntityListeners(SignSessionEntityListener.class)
public class SignSession {

  @Id
  @GeneratedValue(strategy = SEQUENCE)
  @Setter(AccessLevel.NONE)
  long sessionId;

  @NotNull
  @Setter(AccessLevel.NONE)
  @Column(unique = true)
  Long memberId;

  @NotNull
  @Enumerated(EnumType.STRING)
  SignStatus status;

  @CreationTimestamp
  @Setter(AccessLevel.NONE)
  Instant createdAt;

  @UpdateTimestamp
  @Setter(AccessLevel.NONE)
  Instant updatedAt;

  @Embedded BankIdSession bankIdSession;

  public SignSession() {}

  public SignSession(long memberId) {
    this.memberId = memberId;
  }

  public void newOrderStarted(OrderResponse orderReponse) {
    if(bankIdSession == null){
      bankIdSession = new BankIdSession();
    }
    bankIdSession.setOrderReference(orderReponse.getOrderRef());
    bankIdSession.setAutoStartToken(orderReponse.getAutoStartToken());
    status = SignStatus.IN_PROGRESS;
  }

  public OrderResponse getOrderResponse() {
    if(bankIdSession != null){
      return new OrderResponse(bankIdSession.orderReference, bankIdSession.autoStartToken);
    }
    return null;
  }

  public CollectResponse getCollectResponse() {
    if(bankIdSession != null){
      return bankIdSession.getCollectResponse();
    }
    return null;
  }

  public void newCollectResponse(CollectResponse collectResponse) {
    if(bankIdSession == null){
      bankIdSession = new BankIdSession();
    }

    bankIdSession.newCollectResponse(collectResponse);
  }


  public boolean canReuseBankIdSession() {
    if(bankIdSession != null) {
      return bankIdSession.canBeReused();
    }
    return false;
  }
}
