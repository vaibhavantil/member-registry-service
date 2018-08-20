package com.hedvig.memberservice.notificationService.queue;

import lombok.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Value
public class MemberBCCAddress {

  static private Logger log = LoggerFactory.getLogger(MemberBCCAddress.class);

  final String[] parts;

  public MemberBCCAddress(final String emailAddress) {
    this.parts = emailAddress.split("@");
    if(parts.length != 2) {
      throw new IllegalArgumentException("emailAddress is not a valid email address: " + emailAddress);
    }
  }

  public String format(String memberId) {
    return String.format("%s+%s@%s", parts[0], memberId, parts[1]);
  }
}
