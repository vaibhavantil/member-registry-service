package com.hedvig.memberservice.entities;

import static javax.persistence.GenerationType.SEQUENCE;

import com.hedvig.external.bankID.bankIdTypes.OrderResponse;
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
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@EntityListeners(SignSessionEntityListener.class)
public class SignSession {

  @Id
  @GeneratedValue(strategy = SEQUENCE)
  long sessionId;

  @NotNull
  @Column(unique = true)
  Long memberId;

  @NotNull
  @Enumerated(EnumType.STRING)
  SignStatus status;

  @CreationTimestamp
  Instant createdAt;

  @UpdateTimestamp
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

  public Long getSessionId() {
    return sessionId;
  }

  public Long getMemberId() {
    return memberId;
  }

  public void setStatus(SignStatus status) {
    this.status = status;
  }

  public SignStatus getStatus() {
    return status;
  }
}
