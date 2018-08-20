package com.hedvig.memberservice.notificationService.queue;

import lombok.Value;
import lombok.val;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Value
public class MemberBCCAddress {

  static private Logger log = LoggerFactory.getLogger(MemberBCCAddress.class);

  String address;

  public String format(String memberId) {
    val parts = address.split("@");
    if(parts.length != 2) {
      log.error("Address does not seems do be a valid email address: {}", address);
    }
    return String.format("%s+%s@%s", parts[0], memberId, parts[1]);
  }
}
