package com.hedvig.memberservice.web.dto;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class TraceMemberDTO {
  private LocalDateTime date;
  private String oldValue;
  private String newValue;
  private String fieldName;
  private String memberId;
  private String userId;
}
