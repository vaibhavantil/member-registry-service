package com.hedvig.memberservice.web.dto;

import java.util.List;
import lombok.Value;

@Value
public class ChargeMembersDTO {

  private List<Long> memberIds;
}
