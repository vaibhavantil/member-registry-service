package com.hedvig.memberservice.events;

import lombok.Value;

import java.util.HashMap;
import java.util.Map;

@Value
public class NameUpdatedEvent implements Traceable {
  public final Long memberId;
  public final String firstName;
  public final String lastName;

  public NameUpdatedEvent(Long memberId, String firstName, String lastName) {

    this.memberId = memberId;
    this.firstName = firstName;
    this.lastName = lastName;
  }

  @Override
  public Map<String, Object> getValues() {
    Map result = new HashMap();
    result.put("First name", firstName);
    result.put("Last name", lastName);
    return result;
  }
}
