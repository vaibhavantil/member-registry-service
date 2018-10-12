package com.hedvig.memberservice.web.v2.dto;

import com.google.common.net.InetAddresses;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import lombok.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Value
public class WebsignRequest {

  private Logger logger = LoggerFactory.getLogger(WebsignRequest.class);

  @Email
  String email;

  @NotBlank
  String ssn;

  @NotBlank
  String ipAddress;

  public WebsignRequest(@Email String email, @NotBlank String ssn, @NotBlank String ipAddress) {
    this.email = email;
    this.ssn = ssn;
    this.ipAddress = GetValidIpAddress(ipAddress);
  }

  private String GetValidIpAddress(String ipAddress) {
    boolean isValid = InetAddresses.isInetAddress(ipAddress);
    logger.info("Validating ip {}, isValid {}", ipAddress, isValid);
    return isValid ? ipAddress : "1.1.1.1";
  }
}
