package com.hedvig.memberservice.notificationService.queue.requests;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class SendOldInsuranceCancellationEmailRequest extends JobRequest {

    String memberId;
    String insurer;
}
