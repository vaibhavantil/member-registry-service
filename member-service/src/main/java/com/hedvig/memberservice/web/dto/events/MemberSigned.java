package com.hedvig.memberservice.web.dto.events;

import lombok.EqualsAndHashCode;
import lombok.Value;

@Value
@EqualsAndHashCode(callSuper = true)
public class MemberSigned extends MemberServiceEventPayload {

    private final String referenceId;

}
