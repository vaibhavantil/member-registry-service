package com.hedvig.memberservice.commands;

public final class CreateMemberCommand {
  private final Long memberId;

  @java.beans.ConstructorProperties({"memberId"})
  public CreateMemberCommand(Long memberId) {
    this.memberId = memberId;
  }

  public Long getMemberId() {
    return this.memberId;
  }

  public boolean equals(final Object o) {
    if (o == this) return true;
    if (!(o instanceof CreateMemberCommand)) return false;
    final CreateMemberCommand other = (CreateMemberCommand) o;
    final Object this$memberId = this.getMemberId();
    final Object other$memberId = other.getMemberId();
    if (this$memberId == null ? other$memberId != null : !this$memberId.equals(other$memberId)) return false;
    return true;
  }

  public int hashCode() {
    final int PRIME = 59;
    int result = 1;
    final Object $memberId = this.getMemberId();
    result = result * PRIME + ($memberId == null ? 43 : $memberId.hashCode());
    return result;
  }

  public String toString() {
    return "CreateMemberCommand(memberId=" + this.getMemberId() + ")";
  }
}
