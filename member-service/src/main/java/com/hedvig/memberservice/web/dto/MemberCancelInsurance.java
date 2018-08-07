package com.hedvig.memberservice.web.dto;

import java.time.LocalDate;
import lombok.Value;

@Value
public class MemberCancelInsurance {
  LocalDate cancellationDate;
}
