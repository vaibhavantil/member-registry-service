package com.hedvig.memberservice.notificationService.queue.requests;

import lombok.ToString;
import lombok.Value;

@Value
@ToString(callSuper = true)
public class SendCancellationEmailRequest extends JobRequest {

    String memberId;
    String insurer;

    public SendCancellationEmailRequest(String requestIdParam, String memberId, String insurer) {
        super(requestIdParam);
        this.memberId = memberId;
        this.insurer = insurer;
    }
}
