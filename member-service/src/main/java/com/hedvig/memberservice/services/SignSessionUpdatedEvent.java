package com.hedvig.memberservice.services;

import lombok.Value;

@Value
public class SignSessionUpdatedEvent {

  SignSessionUpdatedEventStatus status;

}
