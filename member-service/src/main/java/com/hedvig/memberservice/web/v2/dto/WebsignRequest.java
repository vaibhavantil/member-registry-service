package com.hedvig.memberservice.web.v2.dto;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import lombok.Value;

@Value
public class WebsignRequest {

  @Email
  String email;

  @NotBlank
  String ssn;

  @NotBlank
  String ipAddress;

}
