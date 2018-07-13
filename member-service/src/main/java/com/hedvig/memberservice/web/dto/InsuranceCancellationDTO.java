package com.hedvig.memberservice.web.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Value;

@Value
public class InsuranceCancellationDTO {

  public Long memberId;
  public UUID insuranceId;
  public LocalDate cancellationDate;
}
