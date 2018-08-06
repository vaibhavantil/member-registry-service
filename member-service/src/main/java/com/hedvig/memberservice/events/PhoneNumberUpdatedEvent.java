package com.hedvig.memberservice.events;

import lombok.Value;

@Value
public class PhoneNumberUpdatedEvent {
  private final Long id;
  private final String phoneNumber;
}
