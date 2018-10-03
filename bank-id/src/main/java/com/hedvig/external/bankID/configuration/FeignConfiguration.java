package com.hedvig.external.bankID.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hedvig.external.bankID.bankIdRest.BankIdRestErrorDecoder;
import feign.codec.ErrorDecoder;

public class FeignConfiguration {

  ErrorDecoder errorDecoder(ObjectMapper objectMapper) {
    return new BankIdRestErrorDecoder(objectMapper);
  }

}
