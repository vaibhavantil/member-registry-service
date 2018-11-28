package com.hedvig.memberservice.events;

import com.hedvig.memberservice.aggregates.FraudulentStatus;
import lombok.Value;

import java.util.HashMap;
import java.util.Map;

@Value
public class FraudulentStatusUpdatedEvent implements Traceable {
  private Long memberId;
  private FraudulentStatus fraudulentStatus;
  private String fraudulentDescription;

  @Override
  public Map<String, Object> getValues() {
    Map result = new HashMap();
    result.put("Fraudulent status", fraudulentStatus);
    result.put("Fraudulent description", fraudulentDescription);
    return result;
  }
}
