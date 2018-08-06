package com.hedvig.memberservice.query;

import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import lombok.Getter;
import lombok.Setter;

@Entity
public class TrackingIdEntity {
  @Id
  @Getter
  @Setter
  @Column(unique = true)
  private Long memberId;

  @Getter
  @Setter
  @Column(unique = true)
  private UUID trackingId;
}
