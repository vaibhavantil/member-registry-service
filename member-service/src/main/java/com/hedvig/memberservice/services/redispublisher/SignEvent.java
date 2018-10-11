package com.hedvig.memberservice.services.redispublisher;

import lombok.Value;

@Value
public class SignEvent {

  SignSessionUpdatedEvent signStatus;

}
