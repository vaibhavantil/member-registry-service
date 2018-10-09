package com.hedvig.external.bankID.bankIdRest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hedvig.external.bankID.bankIdRestTypes.BankIdRestError;
import com.hedvig.external.bankID.bankIdRestTypes.BankIdRestErrorResponse;
import com.hedvig.external.bankID.bankIdRestTypes.BankIdRestErrorType;
import feign.Response;
import feign.codec.ErrorDecoder;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BankIdRestErrorDecoder implements ErrorDecoder {

  private static final String ALREADY_IN_PROGRESS_LOWER_CASE = "alreadyinprogress";
  private static final String INVALID_PARAMETERS_LOWER_CASE = "invalidparameters";

  private Logger logger = LoggerFactory.getLogger(BankIdRestErrorDecoder.class);
  private ObjectMapper objectMapper;

  public BankIdRestErrorDecoder(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  @Override
  public Exception decode(String methodKey, Response response) {
    logger.trace(
        "BankIdRestErrorDecoder - Something went wrong with BankId. MethodKey: {}, Response: Status: {}, {}",
        methodKey, response.status(), response);
    switch (response.status()) {
      case 400: {
        BankIdRestErrorResponse res = null;
        try {
          res = objectMapper.readValue(response.body().asReader(), BankIdRestErrorResponse.class);

          switch (res.getErrorCode().toLowerCase()) {
            case ALREADY_IN_PROGRESS_LOWER_CASE:
              return new BankIdRestError(BankIdRestErrorType.ALREADY_IN_PROGRESS,
                  res.getErrorCode(),
                  res.getDetails());
            case INVALID_PARAMETERS_LOWER_CASE:
              return new BankIdRestError(BankIdRestErrorType.INVALID_PARAMETERS,
                  res.getErrorCode(),
                  res.getDetails());
            default:
              return new BankIdRestError(BankIdRestErrorType.UNMAPPED_400, res.getErrorCode(),
                  res.getDetails());
          }
        }
        catch (IOException e) {
          logger.error("Could not read reponse from bankId: " + e.getMessage(), e);
          throw  new RuntimeException(e.getMessage(), e);
        }
      }
      case 401:
        return new BankIdRestError(BankIdRestErrorType.UNAUTHORIZED);
      case 404:
        return new BankIdRestError(BankIdRestErrorType.NOT_FOUND);
      case 408:
        return new BankIdRestError(BankIdRestErrorType.REQUEST_TIMEOUT);
      case 415:
        return new BankIdRestError(BankIdRestErrorType.UNSUPPORTED_MEDIA_TYPE);

      case 500:
        return new BankIdRestError(BankIdRestErrorType.INTERNAL_ERROR);
      case 503:
        return new BankIdRestError(BankIdRestErrorType.MAINTENTANCE);

      default:
        return new BankIdRestError(BankIdRestErrorType.UNKNOWN);
    }
  }

}
