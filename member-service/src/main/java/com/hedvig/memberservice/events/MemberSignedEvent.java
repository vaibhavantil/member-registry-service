package com.hedvig.memberservice.events;

import lombok.Value;

@Value
public class MemberSignedEvent {

  public final Long id;
  public final String referenceId;
  public final String signature;
  public final String oscpResponse;
  public final String ssn;


  /**
   * @deprecated This field exists on some of the events in the database, these events should be cleand up.
   * if you need to get the signedOnDate get the eventTimeStamp instead
   */
  @Deprecated()
  private final String signedOn = null;
}
