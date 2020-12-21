package com.hedvig.memberservice.events;

import lombok.Value;

import java.util.HashMap;
import java.util.Map;

@Value
public class PhoneNumberUpdatedEvent implements Traceable {
  private final Long id;
  private final String phoneNumber;

  @Override
  public Long getMemberId() {
    return id;
  }

  public String getPhoneNumber() {
    return phoneNumber;
  }

  @Override
  public Map<String, Object> getValues() {
    Map result = new HashMap();
    result.put("Phone number", phoneNumber);
    return result;
  }
}
