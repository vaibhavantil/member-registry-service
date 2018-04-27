package com.hedvig.memberservice.notificationService.serviceIntegration.botService.messages;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class BotMessageException extends Exception {

    public BotMessageException(String msg) {
        super(msg);
    }

    public BotMessageException(Throwable t) {
        super(t);
    }

}
