package com.hedvig.memberservice.services.events;

import lombok.Value;

@Value
public class AuthSessionCompleteEvent {

  Long memberId;

}
