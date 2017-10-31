package com.hedvig.memberservice.web.dto.events;

import lombok.Value;

@Value
public class MemberSigned extends MemberServiceEventPayload {

    private final String referenceId;

}
