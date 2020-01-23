package com.hedvig.memberservice.events;

public final class MemberSignedEvent {

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

  @java.beans.ConstructorProperties({"id", "referenceId", "signature", "oscpResponse", "ssn"})
  public MemberSignedEvent(Long id, String referenceId, String signature, String oscpResponse, String ssn) {
    this.id = id;
    this.referenceId = referenceId;
    this.signature = signature;
    this.oscpResponse = oscpResponse;
    this.ssn = ssn;
  }

  public Long getId() {
    return this.id;
  }

  public String getReferenceId() {
    return this.referenceId;
  }

  public String getSignature() {
    return this.signature;
  }

  public String getOscpResponse() {
    return this.oscpResponse;
  }

  public String getSsn() {
    return this.ssn;
  }

  @Deprecated
  public String getSignedOn() {
    return this.signedOn;
  }

  public boolean equals(final Object o) {
    if (o == this) return true;
    if (!(o instanceof MemberSignedEvent)) return false;
    final MemberSignedEvent other = (MemberSignedEvent) o;
    final Object this$id = this.getId();
    final Object other$id = other.getId();
    if (this$id == null ? other$id != null : !this$id.equals(other$id)) return false;
    final Object this$referenceId = this.getReferenceId();
    final Object other$referenceId = other.getReferenceId();
    if (this$referenceId == null ? other$referenceId != null : !this$referenceId.equals(other$referenceId))
      return false;
    final Object this$signature = this.getSignature();
    final Object other$signature = other.getSignature();
    if (this$signature == null ? other$signature != null : !this$signature.equals(other$signature)) return false;
    final Object this$oscpResponse = this.getOscpResponse();
    final Object other$oscpResponse = other.getOscpResponse();
    if (this$oscpResponse == null ? other$oscpResponse != null : !this$oscpResponse.equals(other$oscpResponse))
      return false;
    final Object this$ssn = this.getSsn();
    final Object other$ssn = other.getSsn();
    if (this$ssn == null ? other$ssn != null : !this$ssn.equals(other$ssn)) return false;
    final Object this$signedOn = this.getSignedOn();
    final Object other$signedOn = other.getSignedOn();
    if (this$signedOn == null ? other$signedOn != null : !this$signedOn.equals(other$signedOn)) return false;
    return true;
  }

  public int hashCode() {
    final int PRIME = 59;
    int result = 1;
    final Object $id = this.getId();
    result = result * PRIME + ($id == null ? 43 : $id.hashCode());
    final Object $referenceId = this.getReferenceId();
    result = result * PRIME + ($referenceId == null ? 43 : $referenceId.hashCode());
    final Object $signature = this.getSignature();
    result = result * PRIME + ($signature == null ? 43 : $signature.hashCode());
    final Object $oscpResponse = this.getOscpResponse();
    result = result * PRIME + ($oscpResponse == null ? 43 : $oscpResponse.hashCode());
    final Object $ssn = this.getSsn();
    result = result * PRIME + ($ssn == null ? 43 : $ssn.hashCode());
    final Object $signedOn = this.getSignedOn();
    result = result * PRIME + ($signedOn == null ? 43 : $signedOn.hashCode());
    return result;
  }

  public String toString() {
    return "MemberSignedEvent(id=" + this.getId() + ", referenceId=" + this.getReferenceId() + ", signature=" + this.getSignature() + ", oscpResponse=" + this.getOscpResponse() + ", ssn=" + this.getSsn() + ", signedOn=" + this.getSignedOn() + ")";
  }
}
