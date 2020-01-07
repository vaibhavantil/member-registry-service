package com.hedvig.external.bankID.bankIdTypes;

public class OrderResponse {
  protected String orderRef;
  protected String autoStartToken;

  public OrderResponse(String orderRef, String autoStartToken) {
    this.orderRef = orderRef;
    this.autoStartToken = autoStartToken;
  }

  public String getOrderRef() {
    return orderRef;
  }

  public void setOrderRef(String orderRef) {
    this.orderRef = orderRef;
  }

  public String getAutoStartToken() {
    return autoStartToken;
  }

  public void setAutoStartToken(String autoStartToken) {
    this.autoStartToken = autoStartToken;
  }
}
