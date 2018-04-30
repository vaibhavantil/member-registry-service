package com.hedvig.memberservice.notificationService.queue.requests;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Data;
import lombok.Value;
import lombok.experimental.NonFinal;

@Data
@NonFinal
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = SendOldInsuranceCancellationEmailRequest.class, name = "sendOldInsuranceCancellationEmailRequest"),
        @JsonSubTypes.Type(value = SendActivationEmailRequest.class, name = "sendActivationEmailRequest"),
        @JsonSubTypes.Type(value = SendActivationDateUpdatedRequest.class, name = "sendActivationDateUpdatedRequest")
})
public class JobRequest {
    String requestId;


}
