package com.hedvig.memberservice.events;

import lombok.Value;

@Value
public class MemberSignedEvent {

  private final Long id;
  private final String referenceId;
  private final String signature;
  private final String oscpResponse;


  /**
   * @deprecated This field exists on some of the events in the database, these events should be cleand up.
   * if you need to get the signedOnDate get the eventTimeStamp instead
   */
  @Deprecated()
  private final String signedOn = null;
}
