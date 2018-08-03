package com.hedvig.memberservice.events;

import lombok.Value;

@Value
public class ForgetMeEvent {

  public String memberId;
}
