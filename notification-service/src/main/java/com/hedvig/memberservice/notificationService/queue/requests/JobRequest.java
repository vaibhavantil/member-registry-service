package com.hedvig.memberservice.notificationService.queue.requests;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Value;
import lombok.experimental.NonFinal;

@Value
@NonFinal
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = SendCancellationEmailRequest.class, name = "sendCancellationEmailRequest")
})
public class JobRequest {
    String requestId;
}
