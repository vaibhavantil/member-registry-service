package com.hedvig.external.bankID.bankId;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hedvig.external.bankID.bankIdTypes.BankIdError;
import com.hedvig.external.bankID.bankIdTypes.BankIdErrorResponse;
import com.hedvig.external.bankID.bankIdTypes.BankIdErrorType;
import feign.Response;
import feign.codec.ErrorDecoder;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BankIdErrorDecoder implements ErrorDecoder {

  private static final String ALREADY_IN_PROGRESS_LOWER_CASE = "alreadyinprogress";
  private static final String INVALID_PARAMETERS_LOWER_CASE = "invalidparameters";

  private Logger logger = LoggerFactory.getLogger(BankIdErrorDecoder.class);
  private ObjectMapper objectMapper;

  public BankIdErrorDecoder(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  @Override
  public Exception decode(String methodKey, Response response) {
    logger.trace(
        "BankIdRestErrorDecoder - Something went wrong with BankId. MethodKey: {}, Response: Status: {}, {}",
        methodKey, response.status(), response);
    switch (response.status()) {
      case 400: {
        BankIdErrorResponse res = null;
        try {
          res = objectMapper.readValue(response.body().asReader(), BankIdErrorResponse.class);

          switch (res.getErrorCode().toLowerCase()) {
            case ALREADY_IN_PROGRESS_LOWER_CASE:
              return new BankIdError(BankIdErrorType.ALREADY_IN_PROGRESS,
                  res.getErrorCode(),
                  res.getDetails());
            case INVALID_PARAMETERS_LOWER_CASE:
              return new BankIdError(BankIdErrorType.INVALID_PARAMETERS,
                  res.getErrorCode(),
                  res.getDetails());
            default:
              return new BankIdError(BankIdErrorType.UNMAPPED_400, res.getErrorCode(),
                  res.getDetails());
          }
        }
        catch (IOException e) {
          logger.error("Could not read reponse from bankId: " + e.getMessage(), e);
          throw  new RuntimeException(e.getMessage(), e);
        }
      }
      case 401:
        return new BankIdError(BankIdErrorType.UNAUTHORIZED);
      case 404:
        return new BankIdError(BankIdErrorType.NOT_FOUND);
      case 408:
        return new BankIdError(BankIdErrorType.REQUEST_TIMEOUT);
      case 415:
        return new BankIdError(BankIdErrorType.UNSUPPORTED_MEDIA_TYPE);

      case 500:
        return new BankIdError(BankIdErrorType.INTERNAL_ERROR);
      case 503:
        return new BankIdError(BankIdErrorType.MAINTENTANCE);

      default:
        return new BankIdError(BankIdErrorType.UNKNOWN);
    }
  }

}
