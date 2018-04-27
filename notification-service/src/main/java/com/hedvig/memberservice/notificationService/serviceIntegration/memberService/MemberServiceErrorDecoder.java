package com.hedvig.memberservice.notificationService.serviceIntegration.memberService;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hedvig.memberservice.notificationService.serviceIntegration.memberService.dto.APIErrorDTO;
import com.hedvig.memberservice.notificationService.serviceIntegration.memberService.exceptions.BankIdError;
import com.hedvig.memberservice.notificationService.serviceIntegration.memberService.exceptions.ErrorType;
import feign.Response;
import feign.codec.ErrorDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import static feign.FeignException.errorStatus;

public class MemberServiceErrorDecoder implements ErrorDecoder {

    private final Logger log = LoggerFactory.getLogger(MemberServiceErrorDecoder.class);
    private final ObjectMapper mapper;

    MemberServiceErrorDecoder(ObjectMapper mapper) {

        this.mapper = mapper;
    }

    @Override
    public Exception decode(String methodKey, Response response)  {
        try {
            try {
                APIErrorDTO error = mapper.readValue(response.body().asInputStream(), APIErrorDTO.class);
                ErrorType errorType = ErrorType.valueOf(error.getCode());
                return new BankIdError(errorType, error.getMessage());
            } catch (IllegalArgumentException|JsonParseException |JsonMappingException ex) {
                log.error(String.format("Could not read APIError: %s", ex.getMessage()), ex);
            }
        } catch (IOException ex) {
            log.error(String.format("IO error when decoding memberServiceError:", ex.getMessage()), ex);
        }
        return errorStatus(methodKey, response);
    }
}
