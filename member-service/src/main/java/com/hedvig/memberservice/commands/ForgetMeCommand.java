package com.hedvig.memberservice.commands;

import lombok.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Value
public class ForgetMeCommand {
  private static Logger log = LoggerFactory.getLogger(ForgetMeCommand.class);

  private String memberId;
}
