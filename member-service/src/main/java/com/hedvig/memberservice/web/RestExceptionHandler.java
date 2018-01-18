package com.hedvig.memberservice.web;

import com.hedvig.external.bankID.exceptions.BankIDError;
import com.hedvig.memberservice.web.dto.APIErrorDTO;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@Order(Ordered.HIGHEST_PRECEDENCE)
@ControllerAdvice
public class RestExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(BankIDError.class)
    protected ResponseEntity<?> handleBankIDError(BankIDError error) {
        HttpStatus code = HttpStatus.INTERNAL_SERVER_ERROR;
        APIErrorDTO apiError = new APIErrorDTO(code, error.errorType.name(), error.detail);
        return ResponseEntity.status(code).body(apiError);
    }


}
