package com.hedvig.memberservice.notificationService.serviceIntegration.botService.messages.dto;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Value;

@AllArgsConstructor
@Value
public class BackOfficeMessage {

    private String userId;
    private JsonNode msg;

}
