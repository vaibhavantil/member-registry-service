package com.hedvig.memberservice.web.dto;

public class BankIdAuthRequest {

  private String ipAddress;
  private String memberId;

  public BankIdAuthRequest(String ipAddress, String memberId) {
    this.ipAddress = ipAddress;
    this.memberId = memberId;
  }

  public String getIpAddress() {
    return ipAddress;
  }

  public void setIpAddress(String ipAddress) {
    this.ipAddress = ipAddress;
  }

  public String getMemberId() {
    return memberId;
  }

  public void setMemberId(String memberId) {
    this.memberId = memberId;
  }
}
