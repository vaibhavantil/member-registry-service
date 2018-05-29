package com.hedvig.memberservice.commands;

import com.hedvig.memberservice.query.MemberEntity;
import com.hedvig.memberservice.web.dto.InternalMember;
import com.hedvig.memberservice.web.dto.Member;
import lombok.Value;
import org.axonframework.commandhandling.TargetAggregateIdentifier;

@Value
public class EditMemberInformationCommand {

    @TargetAggregateIdentifier
    Long id;

    InternalMember member;

    public  EditMemberInformationCommand(String id, InternalMember updatedMember){
        this.id = Long.parseLong(id);
        this.member = updatedMember;
    }
}

