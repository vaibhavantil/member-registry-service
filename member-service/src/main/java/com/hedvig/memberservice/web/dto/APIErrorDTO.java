package com.hedvig.memberservice.web.dto;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Value;
import org.springframework.http.HttpStatus;

@Value
@JsonTypeName("apiError")
@JsonTypeInfo(include = JsonTypeInfo.As.WRAPPER_OBJECT, use = JsonTypeInfo.Id.NAME)
public class APIErrorDTO {
    HttpStatus status;
    String code;
    String message;
}
