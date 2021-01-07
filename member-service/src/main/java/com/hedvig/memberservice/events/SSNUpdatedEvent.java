package com.hedvig.memberservice.events;

import lombok.Value;
import lombok.experimental.NonFinal;
import org.axonframework.serialization.Revision;

@Value
@NonFinal
@Revision("1.0")
public class SSNUpdatedEvent {
  private final Long memberId;
  private final String ssn;
  private final Nationality nationality;

  public SSNUpdatedEvent(Long memberId, String ssn, Nationality nationality) {
    this.memberId = memberId;
    this.ssn = ssn;
    this.nationality = nationality;
  }

  public enum Nationality {
      SWEDEN,
      NORWAY,
      DENMARK
  }

  public String getSsn() {
    return ssn;
  }
}
