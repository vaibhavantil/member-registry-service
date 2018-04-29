package com.hedvig.memberservice.notificationService.queue.requests;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.Value;

@Value
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class SendActivationEmailRequest extends JobRequest{
    String memberId;

    public SendActivationEmailRequest(final String requestId, final String memberId) {
        super(requestId);
        this.memberId = memberId;
    }
}
