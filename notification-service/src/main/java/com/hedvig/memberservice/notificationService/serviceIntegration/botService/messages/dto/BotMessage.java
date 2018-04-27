package com.hedvig.memberservice.notificationService.serviceIntegration.botService.messages.dto;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Getter;

import java.time.Instant;
import java.util.Objects;



@Getter
public class BotMessage {

    private JsonNode message;

    private Instant timestamp;

    private Long globalId;

    private Long messageId;

    private Long fromId;

    private JsonNode body;

    private JsonNode header;

    private String type;

    private String id;

    private String hid;



}
