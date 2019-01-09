package com.hedvig.memberservice.events;

import lombok.Value;

import java.util.HashMap;
import java.util.Map;

@Value
public class MemberAuthenticatedEvent implements Traceable{
  private Long memberId;
  private String bankIdReferenceToken;

  @Override
  public Map<String, Object> getValues() {
    Map result = new HashMap();
    result.put("Member authenticated", "");
    return result;
  }
}
