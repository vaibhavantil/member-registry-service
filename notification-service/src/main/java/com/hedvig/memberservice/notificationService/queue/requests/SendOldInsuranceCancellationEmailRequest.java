package com.hedvig.memberservice.notificationService.queue.requests;

import javax.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class SendOldInsuranceCancellationEmailRequest extends JobRequest {

    @NotNull
    String insurer;
}
