package com.hedvig.memberservice.query;
import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Data
public class TraceMemberEntity {
  @Id
  @GeneratedValue
  private Long id;
  private LocalDateTime date;
  private String oldValue;
  private String newValue;
  private String fieldName;
  private Long memberId;
  private String userId;
}
