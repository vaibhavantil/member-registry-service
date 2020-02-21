package com.hedvig.memberservice.web.v2.dto;

import com.google.common.net.InetAddresses;

import java.beans.ConstructorProperties;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WebsignRequest {

  private Logger logger = LoggerFactory.getLogger(WebsignRequest.class);

  @Email
  String email;

  @NotBlank
  String ssn;

  @NotBlank
  String ipAddress;

  @ConstructorProperties({"email", "ssn", "ipAddress"})
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

  public Logger getLogger() {
    return this.logger;
  }

  public @Email String getEmail() {
    return this.email;
  }

  public @NotBlank String getSsn() {
    return this.ssn;
  }

  public @NotBlank String getIpAddress() {
    return this.ipAddress;
  }

  public boolean equals(Object o) {
    if (o == this) {
      return true;
    }
    if (!(o instanceof WebsignRequest)) {
      return false;
    }
    final WebsignRequest other = (WebsignRequest) o;
    final Object this$logger = this.getLogger();
    final Object other$logger = other.getLogger();
    if (this$logger == null ? other$logger != null : !this$logger.equals(other$logger)) {
      return false;
    }
    final Object this$email = this.getEmail();
    final Object other$email = other.getEmail();
    if (this$email == null ? other$email != null : !this$email.equals(other$email)) {
      return false;
    }
    final Object this$ssn = this.getSsn();
    final Object other$ssn = other.getSsn();
    if (this$ssn == null ? other$ssn != null : !this$ssn.equals(other$ssn)) {
      return false;
    }
    final Object this$ipAddress = this.getIpAddress();
    final Object other$ipAddress = other.getIpAddress();
    if (this$ipAddress == null ? other$ipAddress != null
      : !this$ipAddress.equals(other$ipAddress)) {
      return false;
    }
    return true;
  }

  public int hashCode() {
    final int PRIME = 59;
    int result = 1;
    final Object $logger = this.getLogger();
    result = result * PRIME + ($logger == null ? 43 : $logger.hashCode());
    final Object $email = this.getEmail();
    result = result * PRIME + ($email == null ? 43 : $email.hashCode());
    final Object $ssn = this.getSsn();
    result = result * PRIME + ($ssn == null ? 43 : $ssn.hashCode());
    final Object $ipAddress = this.getIpAddress();
    result = result * PRIME + ($ipAddress == null ? 43 : $ipAddress.hashCode());
    return result;
  }

  public String toString() {
    return "WebsignRequest(logger=" + this.getLogger() + ", email=" + this.getEmail() + ", ssn="
      + this.getSsn() + ", ipAddress=" + this.getIpAddress() + ")";
  }
}
