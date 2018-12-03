package com.hedvig.memberservice.events;

import lombok.Value;

import java.util.HashMap;
import java.util.Map;

@Value
public class EmailUpdatedEvent implements Traceable {
  private final Long id;
  private final String email;

  @Override
  public Long getMemberId() {
    return id;
  }

  @Override
  public Map<String, Object> getValues() {
    Map result = new HashMap();
    result.put("Email", email);
    return result;
  }
}
