package com.hedvig.memberservice.notificationService.dto;

import com.hedvig.memberservice.notificationService.queue.requests.JobRequest;
import lombok.Value;

@Value
public class SendInsuranceActivationRequest extends JobRequest {

    String memberId;

    public SendInsuranceActivationRequest(String requestId, String memberId) {
        super(requestId);
        this.memberId = memberId;
    }
}
