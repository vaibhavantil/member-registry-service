package com.hedvig.memberservice.notificationService.queue.requests;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.Instant;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class SendActivationDateUpdatedRequest extends JobRequest {
        String memberId;
        String insurer;
        Instant activationDate;


}
