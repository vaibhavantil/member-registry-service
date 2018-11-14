package com.hedvig.memberservice.web.dto;

import lombok.Value;

import java.time.LocalDate;
import java.util.UUID;

@Value
public class MemberFraudulentStatusDTO {

  private String fraudulentStatus;
  private String fraudulentStatusDescription;

}
