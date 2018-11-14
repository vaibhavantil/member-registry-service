package com.hedvig.memberservice.commands;

import com.hedvig.memberservice.web.dto.InternalMember;
import lombok.Value;
import org.axonframework.commandhandling.TargetAggregateIdentifier;

@Value
public class EditMemberInformationCommand {

  @TargetAggregateIdentifier Long id;

  InternalMember member;
  String token;

  public EditMemberInformationCommand(String id, InternalMember updatedMember, String token) {
    this.id = Long.parseLong(id);
    this.member = updatedMember;
    this.token = token;
  }
}
