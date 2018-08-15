package com.hedvig.memberservice.web.dto;

import java.util.UUID;
import lombok.Value;

@Value
public class SendSignupRequest {
  public UUID token;
  public String email;
  public String id;
}
