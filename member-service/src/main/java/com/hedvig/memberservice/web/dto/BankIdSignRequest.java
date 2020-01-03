package com.hedvig.memberservice.web.dto;

public class BankIdSignRequest {
  private String ssn;
  private String userMessage;
  private String memberId;

  public BankIdSignRequest(String ssn, String userMessage, String memberId) {
    this.ssn = ssn;
    this.userMessage = userMessage;
    this.memberId = memberId;
  }

  public void setSsn(String ssn) {
    this.ssn = ssn;
  }

  public String getSsn() {
    return this.ssn.replace("-", "");
  }

  public String getUserMessage() {
    return userMessage;
  }

  public void setUserMessage(String userMessage) {
    this.userMessage = userMessage;
  }

  public String getMemberId() {
    return memberId;
  }

  public void setMemberId(String memberId) {
    this.memberId = memberId;
  }
}
