package com.hedvig.memberservice.notificationService.queue.requests;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.Value;

import java.time.Instant;

@Value
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class SendActivationDateUpdatedRequest extends JobRequest {
        String memberId;
        String insurer;
        Instant activationDate;

        public SendActivationDateUpdatedRequest(String requestIdParam, String memberId, String insurer, Instant activationDate) {
            super(requestIdParam);
            this.memberId = memberId;
            this.insurer = insurer;
            this.activationDate = activationDate;
    }
}
