package com.hedvig.memberservice.notificationService.queue.requests;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.Value;

@Value
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class SendOldInsuranceCancellationEmailRequest extends JobRequest {

    String memberId;
    String insurer;

    public SendOldInsuranceCancellationEmailRequest(String requestIdParam, String memberId, String insurer) {
        super(requestIdParam);
        this.memberId = memberId;
        this.insurer = insurer;
    }
}
