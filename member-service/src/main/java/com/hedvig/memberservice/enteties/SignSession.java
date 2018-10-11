package com.hedvig.memberservice.enteties;

import static javax.persistence.GenerationType.SEQUENCE;

import java.time.Instant;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.PostPersist;
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
  Long memberId;


  @NotNull
  @Enumerated(EnumType.STRING)
  SignStatus status;

  String autoStartToken;

  String orderReference;

  @CreationTimestamp
  @Setter(AccessLevel.NONE)
  Instant createdAt;

  @UpdateTimestamp
  @Setter(AccessLevel.NONE)
  Instant updatedAt;

  @Embedded CollectResponse collectResponse;

  SignSession() {}

  public SignSession(long memberId) {
    this.memberId = memberId;
  }

}
