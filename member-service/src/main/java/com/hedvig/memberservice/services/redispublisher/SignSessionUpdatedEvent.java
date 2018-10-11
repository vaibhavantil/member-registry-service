package com.hedvig.memberservice.services.redispublisher;

import lombok.Value;

@Value
public class SignSessionUpdatedEvent {

  SignSessionUpdatedEventStatus status;

}
